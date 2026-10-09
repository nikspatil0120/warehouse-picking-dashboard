
package com.warehouse.picking.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_transaction")
public class StockTransaction {

    public enum TransactionType {
        IN,
        OUT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Item is required")
    @ManyToOne(optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @NotNull(message = "Transaction type is required")
    @Enumerated(EnumType.STRING)
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
