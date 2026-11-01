package fu.he190091.watch.client;

import fu.he190091.watch.dto.BrandDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "brand-service", url = "${brand.service.url:http://localhost:8082}")
public interface BrandClient {

    @GetMapping("/api/brands/{id}")
    BrandDTO getBrandById(@PathVariable("id") Integer id);

    @PostMapping("/api/brands/{id}/increment-watch")
    BrandDTO incrementWatchCount(@PathVariable("id") Integer id);

    @PostMapping("/api/brands/{id}/decrement-watch")
    BrandDTO decrementWatchCount(@PathVariable("id") Integer id);

    @PostMapping("/api/brands/{id}/compensate-increment")
    BrandDTO compensateIncrement(@PathVariable("id") Integer id);

    @PostMapping("/api/brands/{id}/compensate-decrement")
    BrandDTO compensateDecrement(@PathVariable("id") Integer id);
}
