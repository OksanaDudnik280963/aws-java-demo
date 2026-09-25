package com.example.awsdemo.controller;

import com.example.awsdemo.dto.OrderRequest;
import com.example.awsdemo.dto.OrderResponse;
import com.example.awsdemo.entity.Order;
import com.example.awsdemo.service.OrderNotificationService;
import com.example.awsdemo.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Places an order (RDS via JPA), then drops a message on SQS so the order
 * gets marked as processed asynchronously by OrderEventListener — the
 * "controller returns immediately, downstream work happens off the request
 * thread" decoupling pattern.
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderNotificationService orderNotificationService;

    public OrderController(OrderService orderService, OrderNotificationService orderNotificationService) {
        this.orderService = orderService;
        this.orderNotificationService = orderNotificationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse placeOrder(@Valid @RequestBody OrderRequest request) {
        Order order = this.orderService.placeOrder(request.product(), request.quantity());
        this.orderNotificationService.notifyOrderPlaced(order.getId());
        return new OrderResponse(order.getId(), order.getStatus());
    }
}