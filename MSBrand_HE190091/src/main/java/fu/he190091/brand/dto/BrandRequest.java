package fu.he190091.brand.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandRequest {
    @NotBlank(message = "Brand name is required")
    private String brandName;

    @NotBlank(message = "Country of origin is required")
    private String countryOfOrigin;
}
