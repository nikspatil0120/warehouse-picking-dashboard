
package com.warehouse.picking.controller;

import com.warehouse.picking.model.CustomerOrder;
import com.warehouse.picking.model.CustomerOrder.OrderStatus;
import com.warehouse.picking.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderApiController {

    private final OrderService orderService;

    public OrderApiController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<CustomerOrder> getOrders() {
        return orderService.getAllOrders();
    }

    @PostMapping
    public ResponseEntity<?> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        try {
            CustomerOrder order = orderService.createOrder(
                    request.itemId(), request.quantity());

            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", exception.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        try {
            CustomerOrder order =
                    orderService.updateOrderStatus(id, request.status());

            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", exception.getMessage()));
        }
    }

    public record CreateOrderRequest(
            @NotNull(message = "Item ID is required")
            Long itemId,

            @Min(value = 1, message = "Quantity must be at least 1")
            int quantity) {
    }

    public record UpdateStatusRequest(
            @NotNull(message = "Status is required")
            OrderStatus status) {
    }
}
