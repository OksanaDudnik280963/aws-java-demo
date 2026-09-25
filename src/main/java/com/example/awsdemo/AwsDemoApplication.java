package com.example.awsdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Demo application tying together the AWS Java building blocks:
 * - S3   -> file upload/download (FileUploadController)
 * - SQS  -> async order notifications (OrderNotificationService + OrderEventListener)
 * - RDS/JPA -> order persistence (Order entity + OrderRepository)
 * <p>
 * Run locally against LocalStack + a local Postgres — see README.md.
 */
@SpringBootApplication
public class AwsDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AwsDemoApplication.class, args);
    }
}


