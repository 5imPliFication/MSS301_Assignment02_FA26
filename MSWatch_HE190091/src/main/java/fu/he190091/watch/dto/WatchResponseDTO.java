package fu.he190091.watch.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchResponseDTO {
    private Integer watchId;
    private String name;
    private Integer categoryId;
    private String categoryName;
    private Integer brandId;
    private String caseMaterial;
    private String strapMaterial;
    private String waterpMaterial;
    private BigDecimal price;
    private LocalDate releaseDate;
    private Integer stock;
}
