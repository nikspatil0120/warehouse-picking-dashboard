
package com.warehouse.picking.service;

import com.warehouse.picking.model.CustomerOrder;
import com.warehouse.picking.model.CustomerOrder.OrderStatus;
import com.warehouse.picking.model.Item;
import com.warehouse.picking.model.User;
import com.warehouse.picking.repository.CustomerOrderRepository;
import com.warehouse.picking.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final CustomerOrderRepository orderRepository;
    private final ItemRepository itemRepository;

    public OrderService(
            CustomerOrderRepository orderRepository,
            ItemRepository itemRepository) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
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

        // Validate requested quantity against available stock.
        if (quantity > item.getQuantity()) {
            throw new IllegalArgumentException(
                    "Insufficient stock for item " + item.getName()
                    + ". Available: " + item.getQuantity()
                    + ", Requested: " + quantity);
        }

        // Reserve stock when the order is created.
        item.setQuantity(item.getQuantity() - quantity);
        itemRepository.save(item);

        CustomerOrder order = new CustomerOrder(item, quantity);

        return orderRepository.save(order);
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

        // Validate requested quantity against available stock.
        if (quantity > item.getQuantity()) {
            throw new IllegalArgumentException(
                    "Insufficient stock for item " + item.getName()
                    + ". Available: " + item.getQuantity()
                    + ", Requested: " + quantity);
        }

        // Reserve stock when the order is created.
        item.setQuantity(item.getQuantity() - quantity);
        itemRepository.save(item);

        CustomerOrder order = new CustomerOrder(customer, item, quantity);

        return orderRepository.save(order);
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

    @Transactional
    public CustomerOrder updateOrderStatus(
            String orderId, OrderStatus newStatus) {
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
            return orderRepository.save(order);
        }

        throw new IllegalArgumentException(
                "Invalid order status transition: "
                        + currentStatus + " -> " + newStatus);
    }

    @Transactional
    public CustomerOrder cancelOrder(String orderId) {
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

        // Release reserved stock
        Item item = order.getItem();
        item.setQuantity(item.getQuantity() + order.getQuantity());
        itemRepository.save(item);

        // Mark order as cancelled
        order.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }
}
