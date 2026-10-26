package com.warehouse.picking.dto;

import java.time.LocalDateTime;

/**
 * Event published to WebSocket when order status changes
 */
public class OrderStatusChangeEvent {

    private String orderId;
    private String itemSku;
    private String itemName;
    private int quantity;
    private String previousStatus;
    private String newStatus;
    private String changedBy;
    private LocalDateTime changedAt;
    private String eventType; // "ORDER_CREATED", "ORDER_STATUS_CHANGED", "ORDER_CANCELLED"

    public OrderStatusChangeEvent() {
    }

    public OrderStatusChangeEvent(String orderId, String itemSku, String itemName, int quantity,
                                   String previousStatus, String newStatus, String changedBy, String eventType) {
        this.orderId = orderId;
        this.itemSku = itemSku;
        this.itemName = itemName;
        this.quantity = quantity;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.changedAt = LocalDateTime.now();
        this.eventType = eventType;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getItemSku() {
        return itemSku;
    }

    public void setItemSku(String itemSku) {
        this.itemSku = itemSku;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(String previousStatus) {
        this.previousStatus = previousStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}
