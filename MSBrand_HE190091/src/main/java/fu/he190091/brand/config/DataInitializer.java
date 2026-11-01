package fu.he190091.brand.config;

import fu.he190091.brand.entity.WatchBrand;
import fu.he190091.brand.repository.WatchBrandRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final WatchBrandRepository brandRepository;

    public DataInitializer(WatchBrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    @Override
    public void run(String... args) {
        if (brandRepository.count() == 0) {
            log.info("Seeding WatchBrands initial data as required by assignment specification...");

            List<WatchBrand> brands = List.of(
                    WatchBrand.builder()
                            .brandName("Rolex")
                            .countryOfOrigin("Switzerland")
                            .totalWatches(2) // Submariner Date, Daytona
                            .build(),
                    WatchBrand.builder()
                            .brandName("Omega")
                            .countryOfOrigin("Switzerland")
                            .totalWatches(2) // Speedmaster, Seamaster
                            .build(),
                    WatchBrand.builder()
                            .brandName("Seiko")
                            .countryOfOrigin("Japan")
                            .totalWatches(2) // Presage, Prospex
                            .build(),
                    WatchBrand.builder()
                            .brandName("Casio")
                            .countryOfOrigin("Japan")
                            .totalWatches(3) // G-Shock, F-91W, IWC Pilot
                            .build(),
                    WatchBrand.builder()
                            .brandName("TAG Heuer")
                            .countryOfOrigin("Switzerland")
                            .totalWatches(1) // Carrera
                            .build()
            );

            brandRepository.saveAll(brands);
            log.info("Successfully seeded {} brands into DBBrand.", brands.size());
        } else {
            log.info("WatchBrands already populated (count: {}). Skipping seed.", brandRepository.count());
        }
    }
}
