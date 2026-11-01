package fu.he190091.watch.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchCategoryDTO {
    private Integer categoryId;
    private String categoryName;
    private String description;
    private String movementType;
    private String priceRange;
}
