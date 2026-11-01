package fu.he190091.brand.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandDTO {
    private Integer brandId;
    private String brandName;
    private String countryOfOrigin;
    private Integer totalWatches;
}
