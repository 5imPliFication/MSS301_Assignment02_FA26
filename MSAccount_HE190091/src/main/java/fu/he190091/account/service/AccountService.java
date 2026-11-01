package fu.he190091.account.service;

import fu.he190091.account.dto.AccountDTO;
import fu.he190091.account.dto.LoginRequest;
import fu.he190091.account.dto.LoginResponse;
import fu.he190091.account.entity.StoreAccount;
import fu.he190091.account.repository.StoreAccountRepository;
import fu.he190091.account.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AccountService {

    private final StoreAccountRepository accountRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AccountService(StoreAccountRepository accountRepository, JwtTokenProvider jwtTokenProvider) {
        this.accountRepository = accountRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public LoginResponse login(LoginRequest request) {
        StoreAccount account = accountRepository.findByUsernameOrEmail(request.getUsername(), request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        // Match password (plaintext comparison as specified in assignment seeds)
        if (!account.getPassword().equals(request.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        // Check if account is active
        if (Boolean.FALSE.equals(account.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account is inactive. Access denied.");
        }

        String token = jwtTokenProvider.generateToken(account);

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .account(toDTO(account))
                .build();
    }

    public List<AccountDTO> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public AccountDTO getAccountById(Integer id) {
        StoreAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found with id: " + id));
        return toDTO(account);
    }

    public AccountDTO getAccountByUsername(String username) {
        StoreAccount account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found with username: " + username));
        return toDTO(account);
    }

    public AccountDTO toDTO(StoreAccount account) {
        return AccountDTO.builder()
                .accountId(account.getAccountId())
                .username(account.getUsername())
                .email(account.getEmail())
                .role(account.getRole())
                .roleName(AccountDTO.resolveRoleName(account.getRole()))
                .isActive(account.getIsActive())
                .build();
    }
}
