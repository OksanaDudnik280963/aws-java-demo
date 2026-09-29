package com.example.awsdemo.integration;

import com.example.awsdemo.dto.OrderRequest;
import com.example.awsdemo.dto.OrderResponse;
import com.example.awsdemo.entity.Order;
import com.example.awsdemo.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class OrderFlowIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void placingOrder_persistsItAndSqsListenerMarksItProcessed() {
        ResponseEntity<OrderResponse> response =
                this.rest.postForEntity("/orders", new OrderRequest("Widget", 3), OrderResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo("PLACED");
        String orderId = response.getBody().orderId();
        assertThat(orderId).isNotBlank();

        // The controller returns immediately; the SQS listener updates the row asynchronously.
        await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
                assertThat(this.orderRepository.findById(orderId))
                        .get()
                        .extracting(Order::getStatus)
                        .isEqualTo("PROCESSED"));
    }

    @Test
    void placingInvalidOrder_isRejectedWithBadRequest() {
        ResponseEntity<String> response =
                this.rest.postForEntity("/orders", new OrderRequest("", 0), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
