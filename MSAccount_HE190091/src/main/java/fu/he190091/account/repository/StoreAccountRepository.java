package fu.he190091.account.repository;

import fu.he190091.account.entity.StoreAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreAccountRepository extends JpaRepository<StoreAccount, Integer> {
    Optional<StoreAccount> findByUsername(String username);
    Optional<StoreAccount> findByEmail(String email);
    Optional<StoreAccount> findByUsernameOrEmail(String username, String email);
    boolean existsByUsername(String username);
}
