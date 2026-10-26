
package com.warehouse.picking.service;

import com.warehouse.picking.controller.OrderWebSocketController;
import com.warehouse.picking.dto.OrderStatusChangeEvent;
import com.warehouse.picking.model.CustomerOrder;
import com.warehouse.picking.model.CustomerOrder.OrderStatus;
import com.warehouse.picking.model.Item;
import com.warehouse.picking.model.OrderAudit;
import com.warehouse.picking.model.User;
import com.warehouse.picking.repository.CustomerOrderRepository;
import com.warehouse.picking.repository.ItemRepository;
import com.warehouse.picking.repository.OrderAuditRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final CustomerOrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderAuditRepository auditRepository;
    private final OrderWebSocketController wsController;

    public OrderService(
            CustomerOrderRepository orderRepository,
            ItemRepository itemRepository,
            OrderAuditRepository auditRepository,
            OrderWebSocketController wsController) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.auditRepository = auditRepository;
        this.wsController = wsController;
    }

    @Transactional
    public CustomerOrder createOrder(String itemId, int quantity) {
        if (itemId == null) {
            throw new IllegalArgumentException("Item ID is required");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Order quantity must be greater than zero");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Item not found with ID: " + itemId));

        // Validate requested quantity against available stock with optimistic locking
        if (quantity > item.getQuantity()) {
            throw new IllegalArgumentException(
                    "Insufficient stock for item " + item.getName()
                    + ". Available: " + item.getQuantity()
                    + ", Requested: " + quantity);
        }

        // Reserve stock with version check (optimistic locking)
        try {
            item.setQuantity(item.getQuantity() - quantity);
            itemRepository.save(item);
        } catch (OptimisticLockingFailureException e) {
            throw new IllegalArgumentException(
                    "Stock was modified by another user. Please retry the order.");
        }

        CustomerOrder order = new CustomerOrder(item, quantity);
        CustomerOrder savedOrder = orderRepository.save(order);

        // Publish WebSocket event for order creation
        OrderStatusChangeEvent event = new OrderStatusChangeEvent(
                savedOrder.getId(),
                item.getSku(),
                item.getName(),
                quantity,
                null,
                "PENDING",
                "SYSTEM",
                "ORDER_CREATED"
        );
        wsController.notifyOrderStatusChange(event);
        wsController.notifyStockUpdate(itemId, item.getQuantity());

        return savedOrder;
    }

    @Transactional
    public CustomerOrder createOrderForCustomer(User customer, String itemId, int quantity) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer is required");
        }
        if (itemId == null) {
            throw new IllegalArgumentException("Item ID is required");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Order quantity must be greater than zero");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Item not found with ID: " + itemId));

        // Validate requested quantity against available stock
        if (quantity > item.getQuantity()) {
            throw new IllegalArgumentException(
                    "Insufficient stock for item " + item.getName()
                    + ". Available: " + item.getQuantity()
                    + ", Requested: " + quantity);
        }

        // Reserve stock with version check (optimistic locking)
        try {
            item.setQuantity(item.getQuantity() - quantity);
            itemRepository.save(item);
        } catch (OptimisticLockingFailureException e) {
            throw new IllegalArgumentException(
                    "Stock was modified by another user. Please retry the order.");
        }

        CustomerOrder order = new CustomerOrder(customer, item, quantity);
        CustomerOrder savedOrder = orderRepository.save(order);

        // Publish WebSocket events
        OrderStatusChangeEvent event = new OrderStatusChangeEvent(
                savedOrder.getId(),
                item.getSku(),
                item.getName(),
                quantity,
                null,
                "PENDING",
                customer.getUsername(),
                "ORDER_CREATED"
        );
        wsController.notifyOrderStatusChange(event);
        wsController.notifyCustomer(customer.getId(), event);
        wsController.notifyStockUpdate(itemId, item.getQuantity());

        return savedOrder;
    }

    @Transactional(readOnly = true)
    public List<CustomerOrder> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<CustomerOrder> getOrdersByCustomer(User customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer is required");
        }
        return orderRepository.findAllByCustomerOrderByCreatedAtDesc(customer);
    }

    @Transactional(readOnly = true)
    public Optional<CustomerOrder> getOrderByIdAndCustomer(String orderId, User customer) {
        if (orderId == null || customer == null) {
            throw new IllegalArgumentException("Order ID and customer are required");
        }
        return orderRepository.findByIdAndCustomer(orderId, customer);
    }

    @Transactional(readOnly = true)
    public Optional<CustomerOrder> getOrderById(String orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID is required");
        }
        return orderRepository.findById(orderId);
    }

    @Transactional
    public CustomerOrder updateOrderStatus(
            String orderId, OrderStatus newStatus, User changedBy) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Order status is required");
        }

        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order not found with ID: " + orderId));

        OrderStatus currentStatus = order.getStatus();

        if (currentStatus == OrderStatus.COMPLETED
                || currentStatus == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Cannot change a completed or cancelled order");
        }

        if (newStatus == OrderStatus.PENDING) {
            throw new IllegalArgumentException(
                    "An order cannot be moved back to PENDING");
        }

        if (newStatus == OrderStatus.CANCELLED
                || (currentStatus == OrderStatus.PENDING
                    && newStatus == OrderStatus.PICKING)
                || (currentStatus == OrderStatus.PICKING
                    && newStatus == OrderStatus.COMPLETED)) {
            
            order.setStatus(newStatus);
            CustomerOrder updatedOrder = orderRepository.save(order);

            // Create audit record
            OrderAudit audit = new OrderAudit(
                    orderId,
                    currentStatus.toString(),
                    newStatus.toString(),
                    changedBy,
                    null
            );
            auditRepository.save(audit);

            // Publish WebSocket event
            OrderStatusChangeEvent event = new OrderStatusChangeEvent(
                    orderId,
                    order.getItem().getSku(),
                    order.getItem().getName(),
                    order.getQuantity(),
                    currentStatus.toString(),
                    newStatus.toString(),
                    changedBy != null ? changedBy.getUsername() : "SYSTEM",
                    "ORDER_STATUS_CHANGED"
            );
            wsController.notifyOrderStatusChange(event);
            wsController.notifyAdmins(event);
            if (order.getCustomer() != null) {
                wsController.notifyCustomer(order.getCustomer().getId(), event);
            }

            return updatedOrder;
        }

        throw new IllegalArgumentException(
                "Invalid order status transition: "
                        + currentStatus + " -> " + newStatus);
    }

    @Transactional
    public CustomerOrder cancelOrder(String orderId, User cancelledBy) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID is required");
        }

        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order not found with ID: " + orderId));

        OrderStatus currentStatus = order.getStatus();

        // Only allow cancellation from PENDING status
        if (currentStatus != OrderStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Order can only be cancelled in PENDING status. Current status: " + currentStatus);
        }

        // Release reserved stock with optimistic locking
        try {
            Item item = order.getItem();
            item.setQuantity(item.getQuantity() + order.getQuantity());
            itemRepository.save(item);
        } catch (OptimisticLockingFailureException e) {
            throw new IllegalArgumentException(
                    "Stock was modified by another user. Please retry the cancellation.");
        }

        // Mark order as cancelled
        order.setStatus(OrderStatus.CANCELLED);
        CustomerOrder cancelledOrder = orderRepository.save(order);

        // Create audit record
        OrderAudit audit = new OrderAudit(
                orderId,
                currentStatus.toString(),
                OrderStatus.CANCELLED.toString(),
                cancelledBy,
                null
        );
        auditRepository.save(audit);

        // Publish WebSocket event
        OrderStatusChangeEvent event = new OrderStatusChangeEvent(
                orderId,
                order.getItem().getSku(),
                order.getItem().getName(),
                order.getQuantity(),
                currentStatus.toString(),
                OrderStatus.CANCELLED.toString(),
                cancelledBy != null ? cancelledBy.getUsername() : "SYSTEM",
                "ORDER_CANCELLED"
        );
        wsController.notifyOrderStatusChange(event);
        if (order.getCustomer() != null) {
            wsController.notifyCustomer(order.getCustomer().getId(), event);
        }
        wsController.notifyStockUpdate(order.getItem().getId(), order.getItem().getQuantity());

        return cancelledOrder;
    }

    @Transactional(readOnly = true)
    public List<OrderAudit> getOrderAuditTrail(String orderId) {
        return auditRepository.findByOrderIdOrderByChangedAtDesc(orderId);
    }
}

