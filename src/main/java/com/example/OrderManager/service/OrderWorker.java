package com.example.OrderManager.service;

import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.VariablesAsType;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Random;
import java.time.Duration;
import com.example.OrderManager.utils.Utilities;

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

            var command = client.newCompleteCommand(job.getKey())
                            .variables(Map.of("creditStatus", result));

            Utilities.handleAsync(command, client, job, "validate-credit");
        } catch (Exception e) {
            System.err.println("[validate-credit] Local processing error: " + e.getMessage());
            
            client.newFailCommand(job.getKey())
                .retries(job.getRetries() - 1)
                .errorMessage("Local worker failure: " + e.getMessage())
                .send();
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
            var command = client.newCompleteCommand(job.getKey());
            Utilities.handleAsync(command, client, job, "reserve-inventory");
        } catch (Exception e) {
            System.err.println("[reserve-inventory] Local processing error: " + e.getMessage());
            
            client.newFailCommand(job.getKey())
                .retries(job.getRetries() - 1)
                .errorMessage("Local worker failure: " + e.getMessage())
                .send();
        }
    }

    /**
     * Part 2 - Requirement: update-sla-log
     * Demonstrates the non-interrupting event execution.
     */
    @JobWorker(type = "update-sla-log", autoComplete = false)
    public void updateSlaLog(final JobClient client, final ActivatedJob job) {
        try {
            System.out.println("Priority Updated!");
            var command = client.newCompleteCommand(job.getKey());
            Utilities.handleAsync(command, client, job, "update-sla-log");
        } catch (Exception e) {
            System.err.println("[update-sla-log] Local processing error: " + e.getMessage());
            
            client.newFailCommand(job.getKey())
                .retries(job.getRetries() - 1)
                .errorMessage("Local worker failure: " + e.getMessage())
                .send();
        }
    }

    /**
     * Logic for the remaining Service Tasks in BPMN
     */
    @JobWorker(type = "generate-invoice", autoComplete = false)
    public void generateInvoice(final JobClient client, final ActivatedJob job) {
        try {
            System.out.println("Worker: Generating Invoice PDF...");
            var command = client.newCompleteCommand(job.getKey());
            Utilities.handleAsync(command, client, job, "generate-invoice");
        } catch (Exception e) {
            System.err.println("[generate-invoice] Local processing error: " + e.getMessage());
            
            client.newFailCommand(job.getKey())
                .retries(job.getRetries() - 1)
                .errorMessage("Local worker failure: " + e.getMessage())
                .send();
        }
    }

    @JobWorker(type = "send-email", autoComplete = false)
    public void sendEmail(final JobClient client, final ActivatedJob job) {
        try {
            System.out.println("Worker: Sending Email Notification...");
            var command = client.newCompleteCommand(job.getKey());
            Utilities.handleAsync(command, client, job, "send-email");
        } catch (Exception e) {
            System.err.println("[send-email] Local processing error: " + e.getMessage());
            
            client.newFailCommand(job.getKey())
                .retries(job.getRetries() - 1)
                .errorMessage("Local worker failure: " + e.getMessage())
                .send();
        }
    }

    @JobWorker(type = "send-sms", autoComplete = false)
    public void sendSms(final JobClient client, final ActivatedJob job) {
        try {
            System.out.println("Worker: Sending SMS Notification...");
            var command = client.newCompleteCommand(job.getKey());
            Utilities.handleAsync(command, client, job, "send-sms");
        } catch (Exception e) {
            System.err.println("[send-sms] Local processing error: " + e.getMessage());
            
            client.newFailCommand(job.getKey())
                .retries(job.getRetries() - 1)
                .errorMessage("Local worker failure: " + e.getMessage())
                .send();
        }
    }
}
