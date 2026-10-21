
package com.warehouse.picking.service;

import com.warehouse.picking.model.CustomerOrder;
import com.warehouse.picking.model.CustomerOrder.OrderStatus;
import com.warehouse.picking.model.Item;
import com.warehouse.picking.repository.CustomerOrderRepository;
import com.warehouse.picking.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public CustomerOrder createOrder(Long itemId, int quantity) {
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

        CustomerOrder order = new CustomerOrder(item, quantity);

        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<CustomerOrder> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public CustomerOrder updateOrderStatus(Long orderId, OrderStatus newStatus) {
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
}
