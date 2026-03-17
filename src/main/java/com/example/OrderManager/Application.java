package com.example.OrderManager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.camunda.zeebe.spring.client.annotation.Deployment;

@SpringBootApplication
@Deployment(resources = "classpath:order_fulfillment_engine.bpmn") // Deploys file on startup
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
