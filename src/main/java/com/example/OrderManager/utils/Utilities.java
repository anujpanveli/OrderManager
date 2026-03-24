package com.example.OrderManager.utils;

import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.command.FinalCommandStep;
import java.time.Duration;

public class Utilities {
    /**
     * Generic handler to process Job completion/failure asynchronously.
     * Implements Exponential Backoff logic.
     */
    public static <T> void handleAsync(FinalCommandStep<T> command, JobClient client, ActivatedJob job, String workerName) {
        command.send().whenComplete((result, exception) -> {
            if (exception == null) {
                System.out.println(workerName + ": Cloud acknowledged completion for job " + job.getKey());
            } else {
                // Exponential Backoff: (Attempt Number)^2
                // Based on default 3 retries: Attempt 1 = 1m, Attempt 2 = 4m, Attempt 3 = 9m
                long attempt = 3 - job.getRetries() + 1;
                long backoffMinutes = (long) Math.pow(attempt, 2);

                System.err.println(workerName + ": Failed to notify cloud. Backing off for " + backoffMinutes + "m. Error: " + exception.getMessage());

                client.newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .retryBackoff(Duration.ofMinutes(backoffMinutes))
                    .errorMessage(workerName + " failed: " + exception.getMessage())
                    .send();
            }
        });
    }
}
