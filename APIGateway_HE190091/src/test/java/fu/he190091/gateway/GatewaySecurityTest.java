package fu.he190091.gateway;

import fu.he190091.gateway.filter.AuthGlobalFilter;
import fu.he190091.gateway.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GatewaySecurityTest {
    JwtUtil jwt = mock(JwtUtil.class);
    AuthGlobalFilter filter = new AuthGlobalFilter(jwt);

    @Test void publicCatalogForwardsTrustedServiceKey() {
        ReflectionTestUtils.setField(filter, "internalKey", "test-key");
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/watches").header("X-Internal-Key", "forged"));
        filter.filter(exchange, forwarded -> {
            assertThat(forwarded.getRequest().getHeaders().getFirst("X-Internal-Key")).isEqualTo("test-key");
            return Mono.empty();
        }).block();
    }

    @Test void participantEndpointsCannotBeCalledThroughGateway() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/brands/1/increment-watch"));
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        filter.filter(exchange, chain).block();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(chain);
    }

    @Test void anonymousMutationIsRejected() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/watches"));
        filter.filter(exchange, mock(GatewayFilterChain.class)).block();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test void sagaLogsAreNotPublic() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/watches/saga-logs/1"));
        filter.filter(exchange, mock(GatewayFilterChain.class)).block();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
