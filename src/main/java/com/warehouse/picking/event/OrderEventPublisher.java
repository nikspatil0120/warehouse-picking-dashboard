package com.warehouse.picking.event;

import com.warehouse.picking.dto.OrderStatusChangeEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Publishes order events that can be consumed by WebSocket handlers and other listeners
 */
@Service
public class OrderEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public OrderEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publishOrderStatusChange(OrderStatusChangeEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
