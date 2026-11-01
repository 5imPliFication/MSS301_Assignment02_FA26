package fu.he190091.watch.repository;

import fu.he190091.watch.entity.SagaLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SagaLogRepository extends JpaRepository<SagaLog, String> {
    List<SagaLog> findByWatchIdOrderByCreatedAtDesc(Integer watchId);
}
