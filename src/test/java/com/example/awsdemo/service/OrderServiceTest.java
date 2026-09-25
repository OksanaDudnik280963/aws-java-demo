package com.example.awsdemo.service;

import com.example.awsdemo.entity.Order;
import com.example.awsdemo.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void placeOrder_shouldSaveNewOrderWithPlacedStatus() {
        when(this.orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = this.orderService.placeOrder("Widget", 3);

        assertThat(result.getProduct()).isEqualTo("Widget");
        assertThat(result.getQuantity()).isEqualTo(3);
        assertThat(result.getStatus()).isEqualTo("PLACED");
        verify(this.orderRepository).save(any(Order.class));
    }

    @Test
    void markAsProcessed_shouldUpdateStatusWhenOrderExists() {
        Order existing = new Order("Widget", 1);
        when(this.orderRepository.findById("order-1")).thenReturn(Optional.of(existing));

        this.orderService.markAsProcessed("order-1");

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(this.orderRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("PROCESSED");
    }

    @Test
    void markAsProcessed_shouldDoNothingWhenOrderMissing() {
        when(this.orderRepository.findById("missing")).thenReturn(Optional.empty());

        this.orderService.markAsProcessed("missing");

        verify(this.orderRepository, never()).save(any());
    }
}