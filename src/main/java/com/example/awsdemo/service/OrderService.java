package com.example.awsdemo.service;

import com.example.awsdemo.entity.Order;
import com.example.awsdemo.repository.OrderRepository;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order placeOrder(String product, int quantity) {
        Order order = new Order(product, quantity);
        return this.orderRepository.save(order);
    }

    public void markAsProcessed(String orderId) {
        this.orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus("PROCESSED");
            this.orderRepository.save(order);
        });
    }

    public void attachFile(String orderId, String s3Key) {
        this.orderRepository.findById(orderId).ifPresent(order -> {
            order.setAttachmentKey(s3Key);
            this.orderRepository.save(order);
        });
    }
}