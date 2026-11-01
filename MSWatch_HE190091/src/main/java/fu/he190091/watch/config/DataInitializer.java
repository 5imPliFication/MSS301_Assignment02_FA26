package fu.he190091.watch.config;

import fu.he190091.watch.entity.Watch;
import fu.he190091.watch.entity.WatchCategory;
import fu.he190091.watch.repository.WatchCategoryRepository;
import fu.he190091.watch.repository.WatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final WatchCategoryRepository categoryRepository;
    private final WatchRepository watchRepository;

    public DataInitializer(WatchCategoryRepository categoryRepository, WatchRepository watchRepository) {
        this.categoryRepository = categoryRepository;
        this.watchRepository = watchRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedCategories();
        seedWatches();
    }

    private void seedCategories() {
        if (categoryRepository.count() == 0) {
            log.info("Seeding WatchCategories initial data as required by assignment specification...");

            List<WatchCategory> categories = List.of(
                    WatchCategory.builder()
                            .categoryName("Dive Watches")
                            .description("Designed for underwater diving and feature high water resistance.")
                            .movementType("Automatic")
                            .priceRange("$500 - $10,000")
                            .build(),
                    WatchCategory.builder()
                            .categoryName("Chronograph Watches")
                            .description("Watches with a stopwatch function in addition to the main display.")
                            .movementType("Automatic or Quartz")
                            .priceRange("$300 - $8,000")
                            .build(),
                    WatchCategory.builder()
                            .categoryName("Dress Watches")
                            .description("Elegant and simple, intended to be worn with formal wear.")
                            .movementType("Automatic or Manual")
                            .priceRange("$400 - $20,000")
                            .build(),
                    WatchCategory.builder()
                            .categoryName("Digital Watches")
                            .description("Watches with an LCD or LED screen that show time in a digital format.")
                            .movementType("Quartz")
                            .priceRange("$50 - $500")
                            .build(),
                    WatchCategory.builder()
                            .categoryName("Pilot Watches")
                            .description("Originally designed for aviators, featuring large, easy-to-read dials.")
                            .movementType("Automatic")
                            .priceRange("$700 - $15,000")
                            .build()
            );

            categoryRepository.saveAll(categories);
            log.info("Successfully seeded {} categories into DBWatch.", categories.size());
        } else {
            log.info("WatchCategories already populated (count: {}). Skipping seed.", categoryRepository.count());
        }
    }

    private void seedWatches() {
        if (watchRepository.count() == 0) {
            log.info("Seeding Watches initial data as required by assignment specification...");

            List<Watch> watches = List.of(
                    Watch.builder()
                            .name("Submariner Date")
                            .categoryId(1)
                            .brandId(1)
                            .caseMaterial("Oystersteel")
                            .strapMaterial("Oystersteel")
                            .waterpMaterial("300 meters")
                            .price(new BigDecimal("9550.00"))
                            .releaseDate(LocalDate.of(2020, 9, 1))
                            .stock(50)
                            .build(),
                    Watch.builder()
                            .name("Speedmaster Moonwatch Professional")
                            .categoryId(2)
                            .brandId(2)
                            .caseMaterial("Stainless Steel")
                            .strapMaterial("Stainless Steel")
                            .waterpMaterial("50 meters")
                            .price(new BigDecimal("7200.00"))
                            .releaseDate(LocalDate.of(2021, 1, 5))
                            .stock(75)
                            .build(),
                    Watch.builder()
                            .name("Presage Cocktail Time")
                            .categoryId(3)
                            .brandId(3)
                            .caseMaterial("Stainless Steel")
                            .strapMaterial("Leather")
                            .waterpMaterial("50 meters")
                            .price(new BigDecimal("425.00"))
                            .releaseDate(LocalDate.of(2019, 6, 1))
                            .stock(200)
                            .build(),
                    Watch.builder()
                            .name("G-Shock DW-5600E-1V")
                            .categoryId(4)
                            .brandId(4)
                            .caseMaterial("Resin")
                            .strapMaterial("Resin")
                            .waterpMaterial("200 meters")
                            .price(new BigDecimal("69.95"))
                            .releaseDate(LocalDate.of(1996, 6, 12))
                            .stock(500)
                            .build(),
                    Watch.builder()
                            .name("Carrera Calibre Heuer 02")
                            .categoryId(2)
                            .brandId(5)
                            .caseMaterial("Stainless Steel")
                            .strapMaterial("Alligator Leather")
                            .waterpMaterial("100 meters")
                            .price(new BigDecimal("5900.00"))
                            .releaseDate(LocalDate.of(2022, 3, 30))
                            .stock(60)
                            .build(),
                    Watch.builder()
                            .name("Seamaster Diver 300M")
                            .categoryId(1)
                            .brandId(2)
                            .caseMaterial("Stainless Steel")
                            .strapMaterial("Rubber")
                            .waterpMaterial("300 meters")
                            .price(new BigDecimal("5400.00"))
                            .releaseDate(LocalDate.of(2018, 2, 15))
                            .stock(120)
                            .build(),
                    Watch.builder()
                            .name("Prospex SPB143")
                            .categoryId(1)
                            .brandId(3)
                            .caseMaterial("Stainless Steel")
                            .strapMaterial("Stainless Steel")
                            .waterpMaterial("200 meters")
                            .price(new BigDecimal("1200.00"))
                            .releaseDate(LocalDate.of(2020, 7, 1))
                            .stock(150)
                            .build(),
                    Watch.builder()
                            .name("Daytona")
                            .categoryId(2)
                            .brandId(1)
                            .caseMaterial("Oystersteel")
                            .strapMaterial("Oystersteel")
                            .waterpMaterial("100 meters")
                            .price(new BigDecimal("14550.00"))
                            .releaseDate(LocalDate.of(2016, 3, 16))
                            .stock(30)
                            .build(),
                    Watch.builder()
                            .name("F-91W")
                            .categoryId(4)
                            .brandId(4)
                            .caseMaterial("Resin")
                            .strapMaterial("Resin")
                            .waterpMaterial("300 meters")
                            .price(new BigDecimal("19.99"))
                            .releaseDate(LocalDate.of(1989, 1, 1))
                            .stock(1000)
                            .build(),
                    Watch.builder()
                            .name("IWC Pilot's Watch Mark XVIII")
                            .categoryId(5)
                            .brandId(4)
                            .caseMaterial("Stainless Steel")
                            .strapMaterial("Leather")
                            .waterpMaterial("60 meters")
                            .price(new BigDecimal("4500.00"))
                            .releaseDate(LocalDate.of(2016, 1, 18))
                            .stock(80)
                            .build()
            );

            watchRepository.saveAll(watches);
            log.info("Successfully seeded {} watches into DBWatch.", watches.size());
        } else {
            log.info("Watches already populated (count: {}). Skipping seed.", watchRepository.count());
        }
    }
}
