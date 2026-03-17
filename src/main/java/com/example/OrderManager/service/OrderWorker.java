package com.example.OrderManager.service;

import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.VariablesAsType;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Random;

@Component
public class OrderWorker {

    /**
     * Part 2 - Requirement: validate-credit
     * Mock logic that randomly returns "Approved", "Rejected", or "Review".
     */
    @JobWorker(type = "validate-credit", autoComplete = false)
    public void validateCredit(final JobClient client, final ActivatedJob job) {
        try {
            String[] statuses = {"Approved", "Rejected", "Review"};
            String result = statuses[new Random().nextInt(statuses.length)];
            
            System.out.println("Worker: Validating Credit. Result: " + result);

            client.newCompleteCommand(job.getKey())
                .variables(Map.of("creditStatus", result))
                .send()
                .join();
        } catch (Exception e) {
            System.err.println("[validate-credit] Failed to process job " + job.getKey() + ": " + e.getMessage());
            client.newFailCommand(job.getKey())
                .retries(job.getRetries() - 1)
                .errorMessage("validate-credit failed: " + e.getMessage())
                .send()
                .join();
        }
    }

    /**
     * Part 2 - Requirement: reserve-inventory
     * Log a message simulating a database lock.
     */
    @JobWorker(type = "reserve-inventory", autoComplete = false)
    public void reserveInventory(final JobClient client, final ActivatedJob job) {
        try {
            String orderId = (String) job.getVariablesAsMap().getOrDefault("orderId", "UNKNOWN");
            System.out.println("[DB LOCK] Reserving inventory for Order ID: " + orderId);
            client.newCompleteCommand(job.getKey()).send().join();
        } catch (Exception e) {
            System.err.println("[reserve-inventory] Failed to process job " + job.getKey() + ": " + e.getMessage());
            client.newFailCommand(job.getKey())
                .retries(job.getRetries() - 1)
                .errorMessage("reserve-inventory failed: " + e.getMessage())
                .send()
                .join();
        }
    }

    /**
     * Part 2 - Requirement: update-sla-log
     * Demonstrates the non-interrupting event execution.
     */
    @JobWorker(type = "update-sla-log")
    public void updateSlaLog() {
        try {
            System.out.println("Priority Updated!");
        } catch (Exception e) {
            // autoComplete = true, so Zeebe handles job completion automatically.
            // Log the error — Zeebe will retry based on the job's retry config.
            System.err.println("[update-sla-log] Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Logic for the remaining Service Tasks in BPMN
     */
    @JobWorker(type = "generate-invoice")
    public void generateInvoice() {
        try {
            System.out.println("Worker: Generating Invoice PDF...");
        } catch (Exception e) {
            System.err.println("[generate-invoice] Unexpected error: " + e.getMessage());
        }
    }

    @JobWorker(type = "send-email")
    public void sendEmail() {
        try {
            System.out.println("Worker: Sending Email Notification...");
        } catch (Exception e) {
            System.err.println("[send-email] Unexpected error: " + e.getMessage());
        }
    }

    @JobWorker(type = "send-sms")
    public void sendSms() {
        try {
            System.out.println("Worker: Sending SMS Notification...");
        } catch (Exception e) {
            System.err.println("[send-sms] Unexpected error: " + e.getMessage());
        }
    }
}
