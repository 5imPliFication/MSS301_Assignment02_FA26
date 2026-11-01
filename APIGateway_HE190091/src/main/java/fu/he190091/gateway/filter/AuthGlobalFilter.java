package fu.he190091.gateway.filter;

import fu.he190091.gateway.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuthGlobalFilter.class);
    private final JwtUtil jwtUtil;

    public AuthGlobalFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        log.debug("Gateway evaluating request: {} {}", method, path);

        // 1. Bypass authentication for public endpoints:
        // - POST /api/accounts/login
        // - Requirement 1: "List all items in Watches table (...). Note that, No permissions for this function."
        // - Public Brand listing
        if (isPublicEndpoint(path, method)) {
            return chain.filter(exchange);
        }

        // 2. Check Authorization Header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Unauthorized attempt on protected endpoint {} {}: Missing or invalid Authorization header", method, path);
            return onError(exchange, HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header. Please log in.");
        }

        String token = authHeader.substring(7).trim();
        if (!jwtUtil.validateToken(token)) {
            log.warn("Unauthorized attempt on {} {}: Token is invalid or expired", method, path);
            return onError(exchange, HttpStatus.UNAUTHORIZED, "Invalid or expired JWT token.");
        }

        Claims claims = jwtUtil.validateAndGetClaims(token);
        Integer role = jwtUtil.getRole(token);
        String username = jwtUtil.getUsername(token);
        Integer accountId = jwtUtil.getAccountId(token);

        // 3. Enforce Administrator role for restricted actions:
        // - POST, PUT, DELETE on /api/watches/** (Requirements 2, 3, 4)
        // - Administrator role is 1
        if (requiresAdminRole(path, method)) {
            if (role == null || role != 1) {
                log.warn("Forbidden access attempt by user '{}' (role: {}) on admin endpoint {} {}", username, role, method, path);
                return onError(exchange, HttpStatus.FORBIDDEN,
                        "Access Denied: Administrator role (1) is strictly required for this operation.");
            }
        }

        // 4. Mutate request with authenticated user claims and propagate downstream
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Id", accountId != null ? String.valueOf(accountId) : "")
                .header("X-User-Name", username != null ? username : "")
                .header("X-User-Role", role != null ? String.valueOf(role) : "")
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private boolean isPublicEndpoint(String path, HttpMethod method) {
        // Login endpoint
        if (path.equals("/api/accounts/login")) {
            return true;
        }

        // Token validation endpoint
        if (path.equals("/api/accounts/validate")) {
            return true;
        }

        // Requirement 1: Public watch listing & details
        if (HttpMethod.GET.equals(method) && path.startsWith("/api/watches")) {
            return true;
        }

        // Public brand listing & details
        if (HttpMethod.GET.equals(method) && path.startsWith("/api/brands")) {
            return true;
        }

        return false;
    }

    private boolean requiresAdminRole(String path, HttpMethod method) {
        // Requirements 2, 3, 4: Add, Update, Delete in Watches table require Administrator role
        if (path.startsWith("/api/watches") && (
                HttpMethod.POST.equals(method) ||
                HttpMethod.PUT.equals(method) ||
                HttpMethod.DELETE.equals(method))) {
            return true;
        }

        // Brand mutation operations
        if (path.startsWith("/api/brands") && (
                HttpMethod.POST.equals(method) && !path.contains("/increment") && !path.contains("/decrement") && !path.contains("/compensate") ||
                HttpMethod.PUT.equals(method) ||
                HttpMethod.DELETE.equals(method))) {
            return true;
        }

        return false;
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String json = String.format(
                "{\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
                status.value(),
                status.getReasonPhrase(),
                message,
                exchange.getRequest().getURI().getPath()
        );

        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // High priority filter before route forwarding
    }
}
