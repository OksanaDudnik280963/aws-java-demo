package com.example.awsdemo.listener;

import com.example.awsdemo.service.OrderService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private final OrderService orderService;

    public OrderEventListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @SqsListener("${app.sqs.order-queue}")
    public void handleOrderEvent(String orderId) {
        this.orderService.markAsProcessed(orderId);
    }
}