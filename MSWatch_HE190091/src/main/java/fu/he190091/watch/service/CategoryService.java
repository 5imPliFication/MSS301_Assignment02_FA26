package fu.he190091.watch.service;

import fu.he190091.watch.dto.WatchCategoryDTO;
import fu.he190091.watch.entity.WatchCategory;
import fu.he190091.watch.repository.WatchCategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoryService {

    private final WatchCategoryRepository categoryRepository;

    public CategoryService(WatchCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<WatchCategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public WatchCategoryDTO getCategoryById(Integer id) {
        WatchCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with ID: " + id));
        return toDTO(category);
    }

    public WatchCategoryDTO toDTO(WatchCategory category) {
        return WatchCategoryDTO.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .description(category.getDescription())
                .movementType(category.getMovementType())
                .priceRange(category.getPriceRange())
                .build();
    }
}
