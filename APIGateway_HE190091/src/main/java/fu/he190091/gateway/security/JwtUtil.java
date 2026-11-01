package fu.he190091.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUtil {

    private final SecretKey key;

    public JwtUtil(@Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims validateAndGetClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            validateAndGetClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Integer getRole(String token) {
        Object role = validateAndGetClaims(token).get("role");
        if (role instanceof Number number) {
            return number.intValue();
        }
        return null;
    }

    public String getUsername(String token) {
        return validateAndGetClaims(token).getSubject();
    }

    public Integer getAccountId(String token) {
        Object accountId = validateAndGetClaims(token).get("accountId");
        if (accountId instanceof Number number) {
            return number.intValue();
        }
        return null;
    }
}
