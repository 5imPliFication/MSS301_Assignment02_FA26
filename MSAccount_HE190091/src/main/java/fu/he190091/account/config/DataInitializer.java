package fu.he190091.account.config;

import fu.he190091.account.entity.StoreAccount;
import fu.he190091.account.repository.StoreAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final StoreAccountRepository accountRepository;

    public DataInitializer(StoreAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public void run(String... args) {
        if (accountRepository.count() == 0) {
            log.info("Seeding StoreAccounts initial data as required by assignment specification...");

            List<StoreAccount> accounts = List.of(
                    StoreAccount.builder()
                            .username("adminsys")
                            .email("admin@drawingpen.com.vn")
                            .password("@1")
                            .role(1) // Administrator
                            .isActive(true)
                            .build(),
                    StoreAccount.builder()
                            .username("staff02")
                            .email("staff02@drawingpen.com.vn")
                            .password("@1")
                            .role(2) // Staff
                            .isActive(true)
                            .build(),
                    StoreAccount.builder()
                            .username("member01")
                            .email("member01@drawingpen.com.vn")
                            .password("@1")
                            .role(3) // Member
                            .isActive(true)
                            .build(),
                    StoreAccount.builder()
                            .username("member02")
                            .email("member02@drawingpen.com.vn")
                            .password("@1")
                            .role(3) // Member
                            .isActive(false)
                            .build(),
                    StoreAccount.builder()
                            .username("staff01")
                            .email("staff01@drawingpen.com.vn")
                            .password("@1")
                            .role(2) // Staff
                            .isActive(false)
                            .build()
            );

            accountRepository.saveAll(accounts);
            log.info("Successfully seeded {} accounts into DBAccount.", accounts.size());
        } else {
            log.info("StoreAccounts already populated (count: {}). Skipping seed.", accountRepository.count());
        }
    }
}
