
package com.warehouse.picking.controller;

import com.warehouse.picking.model.CustomerOrder;
import com.warehouse.picking.model.CustomerOrder.OrderStatus;
import com.warehouse.picking.model.User;
import com.warehouse.picking.service.OrderService;
import com.warehouse.picking.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderApiController {

    private final OrderService orderService;
    private final UserService userService;

    public OrderApiController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
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
            @PathVariable String id,
            @Valid @RequestBody UpdateStatusRequest request,
            Authentication authentication) {
        try {
            // Get authenticated user
            User changedBy = null;
            if (authentication != null) {
                String username = authentication.getName();
                Optional<User> userOpt = userService.findByUsername(username);
                changedBy = userOpt.orElse(null);
            }

            CustomerOrder order =
                    orderService.updateOrderStatus(id, request.status(), changedBy);

            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", exception.getMessage()));
        }
    }

    public record CreateOrderRequest(
            @NotNull(message = "Item ID is required")
            String itemId,

            @Min(value = 1, message = "Quantity must be at least 1")
            int quantity) {
    }

    public record UpdateStatusRequest(
            @NotNull(message = "Status is required")
            OrderStatus status) {
    }
}
