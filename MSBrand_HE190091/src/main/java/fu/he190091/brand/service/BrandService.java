package fu.he190091.brand.service;

import fu.he190091.brand.dto.BrandDTO;
import fu.he190091.brand.dto.BrandRequest;
import fu.he190091.brand.entity.WatchBrand;
import fu.he190091.brand.repository.WatchBrandRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BrandService {

    private final WatchBrandRepository brandRepository;

    public BrandService(WatchBrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    public List<BrandDTO> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public BrandDTO getBrandById(Integer id) {
        WatchBrand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found with ID: " + id));
        return toDTO(brand);
    }

    @Transactional
    public BrandDTO createBrand(BrandRequest request) {
        WatchBrand brand = WatchBrand.builder()
                .brandName(request.getBrandName().trim())
                .countryOfOrigin(request.getCountryOfOrigin().trim())
                .totalWatches(0)
                .build();
        return toDTO(brandRepository.save(brand));
    }

    @Transactional
    public BrandDTO updateBrand(Integer id, BrandRequest request) {
        WatchBrand brand = brandRepository.findForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found with ID: " + id));
        brand.setBrandName(request.getBrandName().trim());
        brand.setCountryOfOrigin(request.getCountryOfOrigin().trim());
        return toDTO(brandRepository.save(brand));
    }

    @Transactional
    public void deleteBrand(Integer id) {
        WatchBrand brand = brandRepository.findForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found with ID: " + id));
        if (brand.getTotalWatches() != 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete a brand referenced by watches");
        }
        brandRepository.delete(brand);
    }

    // ================= Saga Pattern Participant Methods =================

    @Transactional
    public BrandDTO incrementWatchCount(Integer id) {
        WatchBrand brand = brandRepository.findForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found with ID: " + id));
        brand.setTotalWatches(brand.getTotalWatches() + 1);
        return toDTO(brandRepository.save(brand));
    }

    @Transactional
    public BrandDTO decrementWatchCount(Integer id) {
        WatchBrand brand = brandRepository.findForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found with ID: " + id));
        if (brand.getTotalWatches() <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Brand watch count cannot become negative");
        }
        brand.setTotalWatches(brand.getTotalWatches() - 1);
        return toDTO(brandRepository.save(brand));
    }

    @Transactional
    public BrandDTO compensateIncrement(Integer id) {
        WatchBrand brand = brandRepository.findForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found with ID: " + id));
        if (brand.getTotalWatches() <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Brand watch count cannot become negative");
        }
        brand.setTotalWatches(brand.getTotalWatches() - 1);
        return toDTO(brandRepository.save(brand));
    }

    @Transactional
    public BrandDTO compensateDecrement(Integer id) {
        WatchBrand brand = brandRepository.findForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found with ID: " + id));
        brand.setTotalWatches(brand.getTotalWatches() + 1);
        return toDTO(brandRepository.save(brand));
    }

    public BrandDTO toDTO(WatchBrand brand) {
        return BrandDTO.builder()
                .brandId(brand.getBrandId())
                .brandName(brand.getBrandName())
                .countryOfOrigin(brand.getCountryOfOrigin())
                .totalWatches(brand.getTotalWatches())
                .build();
    }
}
