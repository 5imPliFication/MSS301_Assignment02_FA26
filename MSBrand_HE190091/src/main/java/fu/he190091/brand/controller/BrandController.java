package fu.he190091.brand.controller;

import fu.he190091.brand.dto.BrandDTO;
import fu.he190091.brand.dto.BrandRequest;
import fu.he190091.brand.service.BrandService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@CrossOrigin(origins = "*")
public class BrandController {

    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @GetMapping
    public ResponseEntity<List<BrandDTO>> getAllBrands() {
        return ResponseEntity.ok(brandService.getAllBrands());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrandDTO> getBrandById(@PathVariable Integer id) {
        return ResponseEntity.ok(brandService.getBrandById(id));
    }

    @PostMapping
    public ResponseEntity<BrandDTO> createBrand(@Valid @RequestBody BrandRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(brandService.createBrand(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BrandDTO> updateBrand(@PathVariable Integer id, @Valid @RequestBody BrandRequest request) {
        return ResponseEntity.ok(brandService.updateBrand(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrand(@PathVariable Integer id) {
        brandService.deleteBrand(id);
        return ResponseEntity.noContent().build();
    }

    // ================= Saga Participant Endpoints =================

    @PostMapping("/{id}/increment-watch")
    public ResponseEntity<BrandDTO> incrementWatchCount(@PathVariable Integer id) {
        return ResponseEntity.ok(brandService.incrementWatchCount(id));
    }

    @PostMapping("/{id}/decrement-watch")
    public ResponseEntity<BrandDTO> decrementWatchCount(@PathVariable Integer id) {
        return ResponseEntity.ok(brandService.decrementWatchCount(id));
    }

    @PostMapping("/{id}/compensate-increment")
    public ResponseEntity<BrandDTO> compensateIncrement(@PathVariable Integer id) {
        return ResponseEntity.ok(brandService.compensateIncrement(id));
    }

    @PostMapping("/{id}/compensate-decrement")
    public ResponseEntity<BrandDTO> compensateDecrement(@PathVariable Integer id) {
        return ResponseEntity.ok(brandService.compensateDecrement(id));
    }
}
