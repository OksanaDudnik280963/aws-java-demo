package com.example.awsdemo.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String product;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private String status; // PLACED, PROCESSED

    @Column(name = "attachment_key")
    private String attachmentKey; // S3 object key, e.g. an invoice or receipt

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected Order() {
        // JPA
    }

    public Order(String product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.status = "PLACED";
    }

    public String getId() {
        return this.id;
    }

    public String getProduct() {
        return this.product;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAttachmentKey() {
        return this.attachmentKey;
    }

    public void setAttachmentKey(String attachmentKey) {
        this.attachmentKey = attachmentKey;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }
}