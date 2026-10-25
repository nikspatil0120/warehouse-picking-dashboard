
package com.warehouse.picking.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Document(collection = "stock_transactions")
public class StockTransaction {

    public enum TransactionType {
        IN,
        OUT
    }

    @Id
    private String id;

    @NotNull(message = "Item is required")
    @DBRef
    private Item item;

    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    @Min(value = 1, message = "Transaction quantity must be at least 1")
    private int quantity;

    private LocalDateTime createdAt;

    private String notes;

    public StockTransaction() {
    }

    public StockTransaction(
            Item item,
            TransactionType type,
            int quantity,
            String notes) {
        this.item = item;
        this.type = type;
        this.quantity = quantity;
        this.notes = notes;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
