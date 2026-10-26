package com.warehouse.picking.controller;

import com.warehouse.picking.dto.OrderStatusChangeEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * Controller for publishing WebSocket messages to subscribed clients
 * Called by services when order events occur
 */
@Controller
public class OrderWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public OrderWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Sends order status change event to all subscribed clients
     */
    public void notifyOrderStatusChange(OrderStatusChangeEvent event) {
        messagingTemplate.convertAndSend("/topic/orders", event);
    }

    /**
     * Sends order event to admin-specific topic
     */
    public void notifyAdmins(OrderStatusChangeEvent event) {
        messagingTemplate.convertAndSend("/topic/admin/orders", event);
    }

    /**
     * Sends order event to customer-specific topic
     */
    public void notifyCustomer(String customerId, OrderStatusChangeEvent event) {
        messagingTemplate.convertAndSend("/topic/customer/" + customerId + "/orders", event);
    }

    /**
     * Sends stock update notification
     */
    public void notifyStockUpdate(String itemId, int remainingQuantity) {
        messagingTemplate.convertAndSend("/topic/stock/" + itemId, 
                new StockUpdateEvent(itemId, remainingQuantity));
    }

    /**
     * Simple stock update event
     */
    public static class StockUpdateEvent {
        private String itemId;
        private int remainingQuantity;
        private long timestamp;

        public StockUpdateEvent(String itemId, int remainingQuantity) {
            this.itemId = itemId;
            this.remainingQuantity = remainingQuantity;
            this.timestamp = System.currentTimeMillis();
        }

        public String getItemId() {
            return itemId;
        }

        public int getRemainingQuantity() {
            return remainingQuantity;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
