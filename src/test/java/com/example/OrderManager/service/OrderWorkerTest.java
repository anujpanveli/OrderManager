package com.example.OrderManager.service;

import com.example.OrderManager.utils.Utilities;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.command.CompleteJobCommandStep1;
import io.camunda.zeebe.client.api.ZeebeFuture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // Change to LENIENT
class OrderWorkerTest {

    @InjectMocks
    private OrderWorker orderWorker;

    @Mock
    private JobClient jobClient;

    @Mock
    private ActivatedJob activatedJob;

    @Mock
    private CompleteJobCommandStep1 completeJobCommandStep1;

    @Mock
    private ZeebeFuture<io.camunda.zeebe.client.api.response.CompleteJobResponse> zeebeFuture;

    private MockedStatic<Utilities> utilitiesMock;

    @BeforeEach
    void setUp() {
        // Setup static mocking for the Utilities class
        utilitiesMock = mockStatic(Utilities.class);
        
        when(activatedJob.getKey()).thenReturn(123L);
        when(jobClient.newCompleteCommand(anyLong())).thenReturn(completeJobCommandStep1);
        when(completeJobCommandStep1.variables(anyMap())).thenReturn(completeJobCommandStep1);
        
        // Ensure .send() returns a mock future to prevent NPEs in async logic
        when(completeJobCommandStep1.send()).thenReturn(zeebeFuture);
    }

    @AfterEach
    void tearDown() {
        // Critical: Close static mock after each test to avoid memory leaks
        utilitiesMock.close();
    }

    @Test
    @DisplayName("validateCredit: should invoke the async utility handler")
    void validateCredit_ShouldInvokeUtility() {
        orderWorker.validateCredit(jobClient, activatedJob);

        // Verify that the worker creates the command and hands it to the utility
        verify(jobClient).newCompleteCommand(123L);
        utilitiesMock.verify(() -> 
            Utilities.handleAsync(any(), eq(jobClient), eq(activatedJob), eq("validate-credit"))
        );
    }

    @Test
    @DisplayName("reserveInventory: should read orderId and invoke utility")
    void reserveInventory_ShouldInvokeUtility() {
        when(activatedJob.getVariablesAsMap()).thenReturn(Map.of("orderId", "ORD-001"));

        orderWorker.reserveInventory(jobClient, activatedJob);

        verify(activatedJob).getVariablesAsMap();
        utilitiesMock.verify(() -> 
            Utilities.handleAsync(any(), eq(jobClient), eq(activatedJob), eq("reserve-inventory"))
        );
    }

    @Test
    @DisplayName("updateSlaLog: now requires jobClient and invokes utility")
    void updateSlaLog_ShouldInvokeUtility() {
        orderWorker.updateSlaLog(jobClient, activatedJob);

        utilitiesMock.verify(() -> 
            Utilities.handleAsync(any(), eq(jobClient), eq(activatedJob), eq("update-sla-log"))
        );
    }

    @Test
    @DisplayName("generateInvoice: should invoke utility asynchronously")
    void generateInvoice_ShouldInvokeUtility() {
        orderWorker.generateInvoice(jobClient, activatedJob);

        utilitiesMock.verify(() -> 
            Utilities.handleAsync(any(), eq(jobClient), eq(activatedJob), eq("generate-invoice"))
        );
    }

    @Test
    @DisplayName("sendEmail: should invoke utility asynchronously")
    void sendEmail_ShouldInvokeUtility() {
        orderWorker.sendEmail(jobClient, activatedJob);

        utilitiesMock.verify(() -> 
            Utilities.handleAsync(any(), eq(jobClient), eq(activatedJob), eq("send-email"))
        );
    }

    @Test
    @DisplayName("sendSms: should invoke utility asynchronously")
    void sendSms_ShouldInvokeUtility() {
        orderWorker.sendSms(jobClient, activatedJob);

        utilitiesMock.verify(() -> 
            Utilities.handleAsync(any(), eq(jobClient), eq(activatedJob), eq("send-sms"))
        );
    }
}