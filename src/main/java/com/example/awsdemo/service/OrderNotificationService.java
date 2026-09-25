package com.example.awsdemo.service;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OrderNotificationService {

    private final SqsTemplate sqsTemplate;
    private final String queueName;

    public OrderNotificationService(SqsTemplate sqsTemplate,
                                    @Value("${app.sqs.order-queue}") String queueName) {
        this.sqsTemplate = sqsTemplate;
        this.queueName = queueName;
    }

    public void notifyOrderPlaced(String orderId) {
        this.sqsTemplate.send(this.queueName, orderId);
    }
}