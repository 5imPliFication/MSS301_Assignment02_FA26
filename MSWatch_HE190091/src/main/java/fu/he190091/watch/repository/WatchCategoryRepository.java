package fu.he190091.watch.repository;

import fu.he190091.watch.entity.WatchCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WatchCategoryRepository extends JpaRepository<WatchCategory, Integer> {
}
