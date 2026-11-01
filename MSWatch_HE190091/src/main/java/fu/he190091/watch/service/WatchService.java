package fu.he190091.watch.service;

import fu.he190091.watch.client.BrandClient;
import fu.he190091.watch.dto.WatchRequestDTO;
import fu.he190091.watch.dto.WatchResponseDTO;
import fu.he190091.watch.entity.SagaLog;
import fu.he190091.watch.entity.Watch;
import fu.he190091.watch.entity.WatchCategory;
import fu.he190091.watch.repository.SagaLogRepository;
import fu.he190091.watch.repository.WatchCategoryRepository;
import fu.he190091.watch.repository.WatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class WatchService {

    private static final Logger log = LoggerFactory.getLogger(WatchService.class);

    private final WatchRepository watchRepository;
    private final WatchCategoryRepository categoryRepository;
    private final SagaLogRepository sagaLogRepository;
    private final BrandClient brandClient;

    public WatchService(WatchRepository watchRepository,
                        WatchCategoryRepository categoryRepository,
                        SagaLogRepository sagaLogRepository,
                        BrandClient brandClient) {
        this.watchRepository = watchRepository;
        this.categoryRepository = categoryRepository;
        this.sagaLogRepository = sagaLogRepository;
        this.brandClient = brandClient;
    }

    /**
     * Requirement 1: List all items in Watches table (includes CategoryName).
     * No permissions for this function. Items displayed newest first.
     */
    public List<WatchResponseDTO> getAllWatches() {
        return watchRepository.findAllWithCategoryOrderByWatchIdDesc().stream()
                .map(this::toDTO)
                .toList();
    }

    public WatchResponseDTO getWatchById(Integer id) {
        Watch watch = watchRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Watch not found with ID: " + id));
        return toDTO(watch);
    }

    /**
     * Requirement 3: Add new item with business validations and Saga coordination.
     */
    @Transactional
    public WatchResponseDTO createWatch(WatchRequestDTO request) {
        validateBusinessRules(request);

        WatchCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Category not found with ID: " + request.getCategoryId()));

        String sagaId = UUID.randomUUID().toString();
        log.info("Starting CreateWatch Saga [{}] for watch: '{}'", sagaId, request.getName());

        recordSagaLog(sagaId, "CREATE_WATCH", null, request.getBrandId(), "STARTED", "Initiating watch creation");

        // Step 1 of Saga: Forward transaction to MSBrand (increment watch count & verify brand)
        try {
            brandClient.incrementWatchCount(request.getBrandId());
            log.info("Saga [{}]: MSBrand updated successfully for brandId {}", sagaId, request.getBrandId());
        } catch (Exception e) {
            String errorMsg = "Brand validation failed in MSBrand: " + e.getMessage();
            log.error("Saga [{}]: {}", sagaId, errorMsg);
            recordSagaLog(sagaId, "CREATE_WATCH", null, request.getBrandId(), "FAILED", errorMsg);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid BrandID or Brand service unavailable: " + request.getBrandId());
        }

        // Step 2 of Saga: Persist Watch in DBWatch
        Watch savedWatch;
        try {
            Watch watch = Watch.builder()
                    .name(request.getName().trim())
                    .categoryId(request.getCategoryId())
                    .brandId(request.getBrandId())
                    .caseMaterial(request.getCaseMaterial().trim())
                    .strapMaterial(request.getStrapMaterial().trim())
                    .waterpMaterial(request.getWaterpMaterial().trim())
                    .price(request.getPrice())
                    .releaseDate(request.getReleaseDate())
                    .stock(request.getStock())
                    .build();

            savedWatch = watchRepository.save(watch);
            savedWatch.setCategory(category);
            log.info("Saga [{}]: Watch persisted with WatchID {}", sagaId, savedWatch.getWatchId());
        } catch (Exception e) {
            log.error("Saga [{}]: Local DB persistence failed, initiating compensating transaction on MSBrand", sagaId, e);
            recordSagaLog(sagaId, "CREATE_WATCH", null, request.getBrandId(), "COMPENSATING", "Local insert failed: " + e.getMessage());

            // Compensating Transaction
            try {
                brandClient.compensateIncrement(request.getBrandId());
                log.info("Saga [{}]: Compensating transaction completed on MSBrand", sagaId);
                recordSagaLog(sagaId, "CREATE_WATCH", null, request.getBrandId(), "COMPENSATED", "Compensated brand count");
            } catch (Exception compEx) {
                log.error("Saga [{}]: Compensating transaction failed on MSBrand!", sagaId, compEx);
            }

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save watch: " + e.getMessage());
        }

        // Saga Completed successfully
        recordSagaLog(sagaId, "CREATE_WATCH", savedWatch.getWatchId(), request.getBrandId(), "COMPLETED", "Watch created successfully");
        return toDTO(savedWatch);
    }

    /**
     * Requirement 4: Update existing item with validations and Saga coordination.
     */
    @Transactional
    public WatchResponseDTO updateWatch(Integer id, WatchRequestDTO request) {
        validateBusinessRules(request);

        Watch existingWatch = watchRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Watch not found with ID: " + id));

        WatchCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Category not found with ID: " + request.getCategoryId()));

        String sagaId = UUID.randomUUID().toString();
        log.info("Starting UpdateWatch Saga [{}] for WatchID {}", sagaId, id);

        recordSagaLog(sagaId, "UPDATE_WATCH", id, request.getBrandId(), "STARTED", "Initiating watch update");

        Integer oldBrandId = existingWatch.getBrandId();
        Integer newBrandId = request.getBrandId();

        // If brand relationship changed, coordinate across both brands via Saga
        if (!oldBrandId.equals(newBrandId)) {
            try {
                // Step 1: Verify and increment new brand
                brandClient.incrementWatchCount(newBrandId);
            } catch (Exception e) {
                recordSagaLog(sagaId, "UPDATE_WATCH", id, newBrandId, "FAILED", "Failed to link new brand: " + e.getMessage());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid new BrandID: " + newBrandId);
            }

            try {
                // Step 2: Decrement old brand
                brandClient.decrementWatchCount(oldBrandId);
            } catch (Exception e) {
                log.warn("Saga [{}]: Decrementing old brand failed, rolling back new brand increment", sagaId);
                brandClient.compensateIncrement(newBrandId);
                recordSagaLog(sagaId, "UPDATE_WATCH", id, newBrandId, "COMPENSATED", "Rollback due to old brand decrement failure");
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to coordinate brand migration");
            }
        } else {
            // Validate brand still exists
            try {
                brandClient.getBrandById(newBrandId);
            } catch (Exception e) {
                recordSagaLog(sagaId, "UPDATE_WATCH", id, newBrandId, "FAILED", "Brand not found: " + newBrandId);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid BrandID: " + newBrandId);
            }
        }

        // Apply updates
        existingWatch.setName(request.getName().trim());
        existingWatch.setCategoryId(request.getCategoryId());
        existingWatch.setCategory(category);
        existingWatch.setBrandId(request.getBrandId());
        existingWatch.setCaseMaterial(request.getCaseMaterial().trim());
        existingWatch.setStrapMaterial(request.getStrapMaterial().trim());
        existingWatch.setWaterpMaterial(request.getWaterpMaterial().trim());
        existingWatch.setPrice(request.getPrice());
        existingWatch.setReleaseDate(request.getReleaseDate());
        existingWatch.setStock(request.getStock());

        Watch updatedWatch = watchRepository.save(existingWatch);
        recordSagaLog(sagaId, "UPDATE_WATCH", id, request.getBrandId(), "COMPLETED", "Watch updated successfully");

        return toDTO(updatedWatch);
    }

    /**
     * Requirement 2: Delete selected item in Watches table with updating related Microservices (Saga).
     */
    @Transactional
    public void deleteWatch(Integer id) {
        Watch watch = watchRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Watch not found with ID: " + id));

        String sagaId = UUID.randomUUID().toString();
        log.info("Starting DeleteWatch Saga [{}] for WatchID {}", sagaId, id);

        recordSagaLog(sagaId, "DELETE_WATCH", id, watch.getBrandId(), "STARTED", "Initiating watch deletion");

        Integer brandId = watch.getBrandId();

        // Step 1: Remove locally
        watchRepository.delete(watch);

        // Step 2: Notify MSBrand to decrement watch count
        try {
            brandClient.decrementWatchCount(brandId);
            log.info("Saga [{}]: Decremented watch count in MSBrand for brandId {}", sagaId, brandId);
            recordSagaLog(sagaId, "DELETE_WATCH", id, brandId, "COMPLETED", "Watch deleted and brand updated");
        } catch (Exception e) {
            log.error("Saga [{}]: MSBrand decrement failed, executing compensation by restoring watch", sagaId, e);
            recordSagaLog(sagaId, "DELETE_WATCH", id, brandId, "COMPENSATING", "Brand decrement failed, restoring watch");

            // Compensating Transaction: Restore watch locally
            watch.setWatchId(null); // allow re-insert or save
            watchRepository.save(watch);
            recordSagaLog(sagaId, "DELETE_WATCH", id, brandId, "COMPENSATED", "Watch restored due to downstream failure");

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to coordinate deletion with BrandService: " + e.getMessage());
        }
    }

    /**
     * Strict assignment validations:
     * - Name > 10 characters
     * - Price >= 100
     * - ReleaseDate == Current date
     */
    private void validateBusinessRules(WatchRequestDTO request) {
        if (request.getName() == null || request.getName().trim().length() <= 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Value for Name is greater than 10 characters.");
        }

        if (request.getPrice() == null || request.getPrice().compareTo(new BigDecimal("100.00")) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Value for Price >= 100.");
        }

        if (request.getReleaseDate() == null || !request.getReleaseDate().isEqual(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Value for ReleaseDate = Current date. Provided: " + request.getReleaseDate() + ", Expected: " + LocalDate.now());
        }
    }

    private void recordSagaLog(String sagaId, String action, Integer watchId, Integer brandId, String status, String details) {
        try {
            SagaLog sagaLog = SagaLog.builder()
                    .sagaId(sagaId)
                    .actionType(action)
                    .watchId(watchId)
                    .brandId(brandId)
                    .status(status)
                    .details(details)
                    .createdAt(LocalDateTime.now())
                    .build();
            sagaLogRepository.save(sagaLog);
        } catch (Exception ex) {
            log.warn("Failed to write SagaLog [{}]: {}", sagaId, ex.getMessage());
        }
    }

    public WatchResponseDTO toDTO(Watch watch) {
        String categoryName = watch.getCategory() != null ? watch.getCategory().getCategoryName() : null;
        if (categoryName == null && watch.getCategoryId() != null) {
            categoryName = categoryRepository.findById(watch.getCategoryId())
                    .map(WatchCategory::getCategoryName)
                    .orElse(null);
        }

        return WatchResponseDTO.builder()
                .watchId(watch.getWatchId())
                .name(watch.getName())
                .categoryId(watch.getCategoryId())
                .categoryName(categoryName)
                .brandId(watch.getBrandId())
                .caseMaterial(watch.getCaseMaterial())
                .strapMaterial(watch.getStrapMaterial())
                .waterpMaterial(watch.getWaterpMaterial())
                .price(watch.getPrice())
                .releaseDate(watch.getReleaseDate())
                .stock(watch.getStock())
                .build();
    }
}
