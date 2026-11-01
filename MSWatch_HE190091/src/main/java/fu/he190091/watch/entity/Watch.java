package fu.he190091.watch.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "Watches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Watch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "WatchID")
    private Integer watchId;

    @Column(name = "Name", length = 255, nullable = false)
    private String name;

    @Column(name = "CategoryID", nullable = false)
    private Integer categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CategoryID", insertable = false, updatable = false)
    private WatchCategory category;

    @Column(name = "BrandID", nullable = false)
    private Integer brandId;

    @Column(name = "CaseMaterial", length = 100, nullable = false)
    private String caseMaterial;

    @Column(name = "StrapMaterial", length = 100, nullable = false)
    private String strapMaterial;

    @Column(name = "WaterpMaterial", length = 50, nullable = false)
    private String waterpMaterial;

    @Column(name = "Price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "ReleaseDate", nullable = false)
    private LocalDate releaseDate;

    @Column(name = "Stock", nullable = false)
    private Integer stock;
}
