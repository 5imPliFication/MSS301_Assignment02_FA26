package fu.he190091.watch.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchRequestDTO {

    @NotBlank(message = "Name is required")
    @Size(min = 11, max = 255, message = "Name must contain 11 to 255 characters")
    private String name;

    @NotNull(message = "CategoryID is required")
    @Min(1)
    private Integer categoryId;

    @NotNull(message = "BrandID is required")
    @Min(1)
    private Integer brandId;

    @NotBlank(message = "CaseMaterial is required")
    @Size(max = 100)
    private String caseMaterial;

    @NotBlank(message = "StrapMaterial is required")
    @Size(max = 100)
    private String strapMaterial;

    @NotBlank(message = "WaterpMaterial is required")
    @Size(max = 50)
    @com.fasterxml.jackson.annotation.JsonAlias("waterResistance")
    private String waterpMaterial;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "100.00", message = "Value for Price must be >= 100")
    @jakarta.validation.constraints.Digits(integer = 8, fraction = 2)
    private BigDecimal price;

    @NotNull(message = "ReleaseDate is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock must be >= 0")
    private Integer stock;
}
