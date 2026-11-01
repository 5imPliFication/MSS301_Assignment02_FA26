package fu.he190091.brand.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "WatchBrands")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchBrand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BrandID")
    private Integer brandId;

    @Column(name = "BrandName", length = 200, nullable = false)
    private String brandName;

    @Column(name = "CountryOfOrigin", length = 50, nullable = false)
    private String countryOfOrigin;

    @Column(name = "TotalWatches", nullable = false)
    @Builder.Default
    private Integer totalWatches = 0;
}
