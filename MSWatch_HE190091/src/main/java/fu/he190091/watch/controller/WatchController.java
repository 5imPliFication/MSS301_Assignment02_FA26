package fu.he190091.watch.controller;

import fu.he190091.watch.dto.WatchCategoryDTO;
import fu.he190091.watch.dto.WatchRequestDTO;
import fu.he190091.watch.dto.WatchResponseDTO;
import fu.he190091.watch.entity.SagaLog;
import fu.he190091.watch.repository.SagaLogRepository;
import fu.he190091.watch.service.CategoryService;
import fu.he190091.watch.service.WatchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/watches")
@CrossOrigin(origins = "*")
public class WatchController {

    private final WatchService watchService;
    private final CategoryService categoryService;
    private final SagaLogRepository sagaLogRepository;

    public WatchController(WatchService watchService,
                           CategoryService categoryService,
                           SagaLogRepository sagaLogRepository) {
        this.watchService = watchService;
        this.categoryService = categoryService;
        this.sagaLogRepository = sagaLogRepository;
    }

    /**
     * Requirement 1: List all items in Watches table.
     * Information includes all information in this table, CategoryName from WatchCategories table.
     * Note that, No permissions for this function.
     * Items are ordered with the newest at the top of the list.
     */
    @GetMapping
    public ResponseEntity<List<WatchResponseDTO>> getAllWatches() {
        return ResponseEntity.ok(watchService.getAllWatches());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WatchResponseDTO> getWatchById(@PathVariable Integer id) {
        return ResponseEntity.ok(watchService.getWatchById(id));
    }

    /**
     * Requirement 3: Add new item with Saga pattern.
     * Must be called with Administrator role (enforced at API Gateway).
     */
    @PostMapping
    public ResponseEntity<WatchResponseDTO> createWatch(@Valid @RequestBody WatchRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(watchService.createWatch(request));
    }

    /**
     * Requirement 4: Update existing item with Saga pattern.
     * Must be called with Administrator role (enforced at API Gateway).
     */
    @PutMapping("/{id}")
    public ResponseEntity<WatchResponseDTO> updateWatch(@PathVariable Integer id,
                                                        @Valid @RequestBody WatchRequestDTO request) {
        return ResponseEntity.ok(watchService.updateWatch(id, request));
    }

    /**
     * Requirement 2: Delete selected item in Watches table with updating other related microservices.
     * Must be called with Administrator role (enforced at API Gateway).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWatch(@PathVariable Integer id) {
        watchService.deleteWatch(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categories")
    public ResponseEntity<List<WatchCategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<WatchCategoryDTO> getCategoryById(@PathVariable Integer id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @GetMapping("/saga-logs/{watchId}")
    public ResponseEntity<List<SagaLog>> getSagaLogs(@PathVariable Integer watchId) {
        return ResponseEntity.ok(sagaLogRepository.findByWatchIdOrderByCreatedAtDesc(watchId));
    }
}
