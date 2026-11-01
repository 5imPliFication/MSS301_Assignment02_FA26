package fu.he190091.account.controller;

import fu.he190091.account.dto.AccountDTO;
import fu.he190091.account.dto.LoginRequest;
import fu.he190091.account.dto.LoginResponse;
import fu.he190091.account.security.JwtTokenProvider;
import fu.he190091.account.service.AccountService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "*")
public class AccountController {

    private final AccountService accountService;
    private final JwtTokenProvider jwtTokenProvider;

    public AccountController(AccountService accountService, JwtTokenProvider jwtTokenProvider) {
        this.accountService = accountService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(accountService.login(request));
    }

    @GetMapping
    public ResponseEntity<List<AccountDTO>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDTO> getAccountById(@PathVariable Integer id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    @GetMapping("/me")
    public ResponseEntity<AccountDTO> getCurrentAccount(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired JWT token");
        }
        String username = jwtTokenProvider.getUsernameFromToken(token);
        return ResponseEntity.ok(accountService.getAccountByUsername(username));
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        Map<String, Object> result = new HashMap<>();
        if (token != null && jwtTokenProvider.validateToken(token)) {
            Claims claims = jwtTokenProvider.validateAndGetClaims(token);
            result.put("valid", true);
            result.put("username", claims.getSubject());
            result.put("role", claims.get("role"));
            result.put("accountId", claims.get("accountId"));
            return ResponseEntity.ok(result);
        }
        result.put("valid", false);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
    }
}
