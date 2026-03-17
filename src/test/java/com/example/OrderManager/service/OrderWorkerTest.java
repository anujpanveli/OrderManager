package com.example.OrderManager.service;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.command.CompleteJobCommandStep1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderWorkerTest {

    @InjectMocks
    private OrderWorker orderWorker;

    @Mock
    private JobClient jobClient;

    @Mock
    private ActivatedJob activatedJob;

    @Mock
    private CompleteJobCommandStep1 completeJobCommandStep1;

    @BeforeEach
    void setUp() {
        when(activatedJob.getKey()).thenReturn(123L);
        when(jobClient.newCompleteCommand(anyLong())).thenReturn(completeJobCommandStep1);
        when(completeJobCommandStep1.variables(anyMap())).thenReturn(completeJobCommandStep1);
        when(completeJobCommandStep1.send()).thenReturn(mock(io.camunda.zeebe.client.api.ZeebeFuture.class));
    }

    // ─────────────────────────────────────────────
    // validate-credit worker tests
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("validateCredit: should complete the job exactly once")
    void validateCredit_ShouldCompleteJobOnce() {
        orderWorker.validateCredit(jobClient, activatedJob);

        verify(jobClient, times(1)).newCompleteCommand(123L);
        verify(completeJobCommandStep1, times(1)).send();
    }

    @RepeatedTest(20)
    @DisplayName("validateCredit: result must always be Approved, Rejected, or Review")
    void validateCredit_ShouldReturnValidCreditStatus() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);

        orderWorker.validateCredit(jobClient, activatedJob);

        verify(completeJobCommandStep1).variables(captor.capture());

        Map<String, Object> vars = captor.getValue();
        assertThat(vars).containsKey("creditStatus");

        String creditStatus = (String) vars.get("creditStatus");
        assertThat(creditStatus)
                .as("creditStatus must be one of the three valid values")
                .isIn(List.of("Approved", "Rejected", "Review"));
    }

    @Test
    @DisplayName("validateCredit: returned variable map must have exactly one entry")
    void validateCredit_ShouldReturnExactlyOneVariable() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);

        orderWorker.validateCredit(jobClient, activatedJob);

        verify(completeJobCommandStep1).variables(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
    }

    // ─────────────────────────────────────────────
    // reserve-inventory worker tests
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("reserveInventory: should complete the job exactly once")
    void reserveInventory_ShouldCompleteJobOnce() {
        when(activatedJob.getVariablesAsMap()).thenReturn(Map.of("orderId", "ORD-001"));
        // reserve-inventory uses autoComplete=false with no variables, stub the chain
        when(completeJobCommandStep1.send()).thenReturn(mock(io.camunda.zeebe.client.api.ZeebeFuture.class));

        orderWorker.reserveInventory(jobClient, activatedJob);

        verify(jobClient, times(1)).newCompleteCommand(123L);
    }

    @Test
    @DisplayName("reserveInventory: should handle missing orderId gracefully with UNKNOWN fallback")
    void reserveInventory_ShouldUseFallbackWhenOrderIdMissing() {
        when(activatedJob.getVariablesAsMap()).thenReturn(Map.of());

        // Should not throw any exception
        orderWorker.reserveInventory(jobClient, activatedJob);

        verify(jobClient, times(1)).newCompleteCommand(123L);
    }

    @Test
    @DisplayName("reserveInventory: should read orderId from job variables")
    void reserveInventory_ShouldReadOrderIdFromVariables() {
        when(activatedJob.getVariablesAsMap()).thenReturn(Map.of("orderId", "ORD-999"));

        orderWorker.reserveInventory(jobClient, activatedJob);

        // verify getVariablesAsMap was called to access orderId
        verify(activatedJob, times(1)).getVariablesAsMap();
    }

    // ─────────────────────────────────────────────
    // update-sla-log worker tests
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("updateSlaLog: should execute without throwing any exception")
    void updateSlaLog_ShouldExecuteWithoutException() {
        // autoComplete = true, so no jobClient interaction is needed
        assertThat(catchThrowable(() -> orderWorker.updateSlaLog()))
                .as("updateSlaLog must not throw")
                .isNull();
    }

    @Test
    @DisplayName("updateSlaLog: should not interact with jobClient (autoComplete=true)")
    void updateSlaLog_ShouldNotInteractWithJobClient() {
        orderWorker.updateSlaLog();
        verifyNoInteractions(jobClient);
    }

    // ─────────────────────────────────────────────
    // generate-invoice worker tests
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("generateInvoice: should execute without throwing any exception")
    void generateInvoice_ShouldExecuteWithoutException() {
        assertThat(catchThrowable(() -> orderWorker.generateInvoice()))
                .as("generateInvoice must not throw")
                .isNull();
    }

    // ─────────────────────────────────────────────
    // send-email worker tests
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("sendEmail: should execute without throwing any exception")
    void sendEmail_ShouldExecuteWithoutException() {
        assertThat(catchThrowable(() -> orderWorker.sendEmail()))
                .as("sendEmail must not throw")
                .isNull();
    }

    // ─────────────────────────────────────────────
    // send-sms worker tests
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("sendSms: should execute without throwing any exception")
    void sendSms_ShouldExecuteWithoutException() {
        assertThat(catchThrowable(() -> orderWorker.sendSms()))
                .as("sendSms must not throw")
                .isNull();
    }

    // ─────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────

    private Throwable catchThrowable(ThrowableAssert.ThrowingCallable callable) {
        try {
            callable.call();
            return null;
        } catch (Throwable t) {
            return t;
        }
    }

    interface ThrowableAssert {
        interface ThrowingCallable {
            void call() throws Throwable;
        }
    }
}
