package fu.he190091.watch;

import fu.he190091.watch.client.BrandClient;
import fu.he190091.watch.dto.WatchRequestDTO;
import fu.he190091.watch.entity.Watch;
import fu.he190091.watch.repository.SagaLogRepository;
import fu.he190091.watch.repository.WatchRepository;
import fu.he190091.watch.service.WatchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@ActiveProfiles("h2")
class WatchSagaTest {
    @Autowired WatchService service;
    @Autowired SagaLogRepository logs;
    @SpyBean WatchRepository watches;
    @MockBean BrandClient brands;

    private WatchRequestDTO request() {
        return WatchRequestDTO.builder().name("Regression Test Watch").categoryId(1).brandId(2)
                .caseMaterial("Steel").strapMaterial("Leather").waterpMaterial("30 meters")
                .price(new BigDecimal("100.00")).releaseDate(LocalDate.now()).stock(1).build();
    }

    @Test void createCommitFailureCompensatesAndRetainsLog() {
        long before = watches.count();
        doThrow(new IllegalStateException("insert failed")).when(watches).saveAndFlush(any(Watch.class));
        assertThatThrownBy(() -> service.createWatch(request())).isInstanceOf(ResponseStatusException.class);
        verify(brands).compensateIncrement(2);
        assertThat(watches.count()).isEqualTo(before);
        assertThat(logs.findAll()).anyMatch(l -> l.getActionType().equals("CREATE_WATCH") && l.getStatus().equals("COMPENSATED"));
    }

    @Test void realDatabaseConstraintFailureCompensatesAndCommitsAudit() {
        var input = request();
        // Bypass controller validation to exercise a real database rollback.
        input.setName("x".repeat(256));
        long before = watches.count();
        assertThatThrownBy(() -> service.createWatch(input)).isInstanceOf(ResponseStatusException.class);
        verify(brands).compensateIncrement(2);
        assertThat(watches.count()).isEqualTo(before);
        assertThat(logs.findAll()).anyMatch(l -> l.getStatus().equals("COMPENSATED"));
    }

    @Test void realUpdateRollbackPreservesOriginalBrand() {
        var input = request();
        input.setPrice(new BigDecimal("100000000.00"));
        assertThatThrownBy(() -> service.updateWatch(1, input)).isInstanceOf(ResponseStatusException.class);
        verify(brands).compensateDecrement(1);
        verify(brands).compensateIncrement(2);
        assertThat(watches.findById(1).orElseThrow().getBrandId()).isEqualTo(1);
    }

    @Test void failedUpdateReversesBothBrandsAndKeepsOriginal() {
        Watch original = watches.findById(1).orElseThrow();
        doThrow(new IllegalStateException("update failed")).when(watches).saveAndFlush(any(Watch.class));
        assertThatThrownBy(() -> service.updateWatch(1, request())).isInstanceOf(ResponseStatusException.class);
        verify(brands).compensateDecrement(1);
        verify(brands).compensateIncrement(2);
        assertThat(watches.findById(1).orElseThrow().getName()).isEqualTo(original.getName());
        assertThat(watches.findById(1).orElseThrow().getBrandId()).isEqualTo(1);
    }

    @Test void failedDeleteKeepsOriginalIdentityAndRestoresBrand() {
        doThrow(new IllegalStateException("delete failed")).when(watches).delete(any(Watch.class));
        assertThatThrownBy(() -> service.deleteWatch(1)).isInstanceOf(ResponseStatusException.class);
        verify(brands).compensateDecrement(1);
        assertThat(watches.existsById(1)).isTrue();
    }

    @Test void unavailableBrandDoesNotDeleteWatch() {
        doThrow(new IllegalStateException("unavailable")).when(brands).decrementWatchCount(1);
        assertThatThrownBy(() -> service.deleteWatch(1)).isInstanceOf(ResponseStatusException.class);
        verify(watches, never()).delete(any(Watch.class));
        assertThat(watches.existsById(1)).isTrue();
    }

    @Test void compensationFailureIsVisibleAndOtherReversalStillRuns() {
        doThrow(new IllegalStateException("update failed")).when(watches).saveAndFlush(any(Watch.class));
        doThrow(new IllegalStateException("brand unavailable")).when(brands).compensateDecrement(1);
        assertThatThrownBy(() -> service.updateWatch(1, request())).isInstanceOf(ResponseStatusException.class);
        verify(brands).compensateIncrement(2);
        assertThat(logs.findByWatchIdOrderByCreatedAtDesc(1).getFirst().getStatus()).isEqualTo("COMPENSATION_FAILED");
    }

    @Test void catalogIncludesCategoryAndExactAssignmentSeed() {
        var catalog = service.getAllWatches();
        assertThat(catalog).hasSize(10);
        assertThat(catalog.getFirst().getWatchId()).isEqualTo(10);
        assertThat(catalog).allMatch(w -> w.getCategoryName() != null);
        assertThat(service.getWatchById(9).getWaterpMaterial()).isEqualTo("30 meters");
    }

    @Test void trimmedNameAndDateRulesApplyToUpdates() {
        var input = request();
        input.setName("          short          ");
        assertThatThrownBy(() -> service.updateWatch(1, input)).isInstanceOf(ResponseStatusException.class);
        input.setName("Valid Watch Name");
        input.setReleaseDate(LocalDate.now().minusDays(1));
        assertThatThrownBy(() -> service.updateWatch(1, input)).isInstanceOf(ResponseStatusException.class);
        verifyNoInteractions(brands);
    }
}
