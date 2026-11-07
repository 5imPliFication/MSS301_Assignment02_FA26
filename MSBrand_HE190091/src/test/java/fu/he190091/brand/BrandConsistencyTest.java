package fu.he190091.brand;

import fu.he190091.brand.dto.BrandRequest;
import fu.he190091.brand.repository.WatchBrandRepository;
import fu.he190091.brand.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("h2")
class BrandConsistencyTest {
    @Autowired BrandService service;
    @Autowired WatchBrandRepository brands;

    @Test void referencedBrandCannotBeDeleted() {
        assertThatThrownBy(() -> service.deleteBrand(1)).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409");
        assertThat(brands.existsById(1)).isTrue();
    }

    @Test void emptyBrandCannotBeDecremented() {
        BrandRequest input = new BrandRequest();
        input.setBrandName("Regression Brand");
        input.setCountryOfOrigin("Vietnam");
        int id = service.createBrand(input).getBrandId();
        assertThatThrownBy(() -> service.decrementWatchCount(id)).isInstanceOf(ResponseStatusException.class);
        assertThat(service.getBrandById(id).getTotalWatches()).isZero();
        service.deleteBrand(id);
    }
}
