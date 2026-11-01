package fu.he190091.brand.repository;

import fu.he190091.brand.entity.WatchBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WatchBrandRepository extends JpaRepository<WatchBrand, Integer> {
    Optional<WatchBrand> findByBrandNameIgnoreCase(String brandName);
}
