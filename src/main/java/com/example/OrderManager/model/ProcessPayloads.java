package com.example.OrderManager.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Use @JsonIgnoreProperties to prevent errors if Camunda sends extra internal variables
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessPayloads {

    // DTO for incoming order data from the Webhook
    public record OrderRequest(
        String orderId, 
        Double amount, 
        String notificationChannels
    ) {}

    // DTO for the automated credit validation worker result
    public record CreditResponse(
        String creditStatus
    ) {}

    // DTO for the manual review result from the Angular app
    public record ReviewResult(
        boolean approved
    ) {}
}