
package com.warehouse.picking.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Document(collection = "customer_orders")
public class CustomerOrder {

    public enum OrderStatus {
        PENDING,
        PICKING,
        COMPLETED,
        CANCELLED
    }

    @Id
    private String id;

    @DBRef
    private User customer;

    @NotNull(message = "Item is required")
    @DBRef
    private Item item;

    @Min(value = 1, message = "Order quantity must be at least 1")
    private int quantity;

    private OrderStatus status = OrderStatus.PENDING;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    public CustomerOrder() {
    }

    public CustomerOrder(Item item, int quantity) {
        this.item = item;
        this.quantity = quantity;
        this.status = OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public CustomerOrder(User customer, Item item, int quantity) {
        this.customer = customer;
        this.item = item;
        this.quantity = quantity;
        this.status = OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
