package com.example.OrderManager.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessPayloadsTest {

    // ─────────────────────────────────────────────
    // OrderRequest record tests
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("OrderRequest: should correctly store all fields via canonical constructor")
    void orderRequest_ShouldStoreAllFields() {
        ProcessPayloads.OrderRequest request =
                new ProcessPayloads.OrderRequest("ORD-001", 250.00, "Email");

        assertThat(request.orderId()).isEqualTo("ORD-001");
        assertThat(request.amount()).isEqualTo(250.00);
        assertThat(request.notificationChannels()).isEqualTo("Email");
    }

    @Test
    @DisplayName("OrderRequest: two records with same values should be equal")
    void orderRequest_ShouldSupportValueEquality() {
        ProcessPayloads.OrderRequest r1 = new ProcessPayloads.OrderRequest("ORD-001", 100.0, "SMS");
        ProcessPayloads.OrderRequest r2 = new ProcessPayloads.OrderRequest("ORD-001", 100.0, "SMS");

        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }

    @Test
    @DisplayName("OrderRequest: two records with different values should not be equal")
    void orderRequest_ShouldDetectInequality() {
        ProcessPayloads.OrderRequest r1 = new ProcessPayloads.OrderRequest("ORD-001", 100.0, "Email");
        ProcessPayloads.OrderRequest r2 = new ProcessPayloads.OrderRequest("ORD-002", 200.0, "SMS");

        assertThat(r1).isNotEqualTo(r2);
    }

    @Test
    @DisplayName("OrderRequest: should support null values without throwing")
    void orderRequest_ShouldHandleNullValues() {
        ProcessPayloads.OrderRequest request =
                new ProcessPayloads.OrderRequest(null, null, null);

        assertThat(request.orderId()).isNull();
        assertThat(request.amount()).isNull();
        assertThat(request.notificationChannels()).isNull();
    }

    @Test
    @DisplayName("OrderRequest: toString should include all field values")
    void orderRequest_ToStringShouldContainFieldValues() {
        ProcessPayloads.OrderRequest request =
                new ProcessPayloads.OrderRequest("ORD-123", 99.99, "Email");

        String str = request.toString();
        assertThat(str).contains("ORD-123");
        assertThat(str).contains("99.99");
        assertThat(str).contains("Email");
    }

    // ─────────────────────────────────────────────
    // CreditResponse record tests
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CreditResponse: should store creditStatus correctly")
    void creditResponse_ShouldStoreCreditStatus() {
        ProcessPayloads.CreditResponse response =
                new ProcessPayloads.CreditResponse("Approved");

        assertThat(response.creditStatus()).isEqualTo("Approved");
    }

    @Test
    @DisplayName("CreditResponse: equality should be based on value")
    void creditResponse_ShouldSupportValueEquality() {
        ProcessPayloads.CreditResponse c1 = new ProcessPayloads.CreditResponse("Rejected");
        ProcessPayloads.CreditResponse c2 = new ProcessPayloads.CreditResponse("Rejected");

        assertThat(c1).isEqualTo(c2);
    }

    @Test
    @DisplayName("CreditResponse: should support all three valid statuses")
    void creditResponse_ShouldAcceptAllValidStatuses() {
        assertThat(new ProcessPayloads.CreditResponse("Approved").creditStatus()).isEqualTo("Approved");
        assertThat(new ProcessPayloads.CreditResponse("Rejected").creditStatus()).isEqualTo("Rejected");
        assertThat(new ProcessPayloads.CreditResponse("Review").creditStatus()).isEqualTo("Review");
    }

    // ─────────────────────────────────────────────
    // ReviewResult record tests
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("ReviewResult: should store approved=true correctly")
    void reviewResult_ShouldStoreApprovedTrue() {
        ProcessPayloads.ReviewResult result = new ProcessPayloads.ReviewResult(true);
        assertThat(result.approved()).isTrue();
    }

    @Test
    @DisplayName("ReviewResult: should store approved=false correctly")
    void reviewResult_ShouldStoreApprovedFalse() {
        ProcessPayloads.ReviewResult result = new ProcessPayloads.ReviewResult(false);
        assertThat(result.approved()).isFalse();
    }

    @Test
    @DisplayName("ReviewResult: equality should distinguish true and false")
    void reviewResult_TrueAndFalseShouldNotBeEqual() {
        ProcessPayloads.ReviewResult approved = new ProcessPayloads.ReviewResult(true);
        ProcessPayloads.ReviewResult rejected = new ProcessPayloads.ReviewResult(false);

        assertThat(approved).isNotEqualTo(rejected);
    }

    @Test
    @DisplayName("ReviewResult: same value should produce equal instances")
    void reviewResult_SameValueShouldBeEqual() {
        ProcessPayloads.ReviewResult r1 = new ProcessPayloads.ReviewResult(true);
        ProcessPayloads.ReviewResult r2 = new ProcessPayloads.ReviewResult(true);

        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }
}
