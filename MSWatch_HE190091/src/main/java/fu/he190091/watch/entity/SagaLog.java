package fu.he190091.watch.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "SagaLogs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SagaLog {

    @Id
    @Column(name = "SagaID", length = 64)
    private String sagaId;

    @Column(name = "ActionType", length = 30, nullable = false)
    private String actionType; // CREATE_WATCH, UPDATE_WATCH, DELETE_WATCH

    @Column(name = "WatchID")
    private Integer watchId;

    @Column(name = "BrandID")
    private Integer brandId;

    @Column(name = "Status", length = 30, nullable = false)
    private String status; // STARTED, COMPLETED, COMPENSATING, COMPENSATED, FAILED

    @Column(name = "Details", length = 1000)
    private String details;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;
}
