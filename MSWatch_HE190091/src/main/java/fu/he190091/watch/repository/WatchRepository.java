package fu.he190091.watch.repository;

import fu.he190091.watch.entity.Watch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WatchRepository extends JpaRepository<Watch, Integer> {
    @Query("SELECT w FROM Watch w LEFT JOIN FETCH w.category ORDER BY w.watchId DESC")
    List<Watch> findAllWithCategoryOrderByWatchIdDesc();
}
