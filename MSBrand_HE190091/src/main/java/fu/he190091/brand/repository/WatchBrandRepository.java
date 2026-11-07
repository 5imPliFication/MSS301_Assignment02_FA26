package fu.he190091.brand.repository;

import fu.he190091.brand.entity.WatchBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WatchBrandRepository extends JpaRepository<WatchBrand, Integer> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select b from WatchBrand b where b.brandId = :id")
    Optional<WatchBrand> findForUpdate(@org.springframework.data.repository.query.Param("id") Integer id);
    Optional<WatchBrand> findByBrandNameIgnoreCase(String brandName);
}
