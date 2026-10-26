
package com.warehouse.picking.controller;

import com.warehouse.picking.model.CustomerOrder;
import com.warehouse.picking.model.CustomerOrder.OrderStatus;
import com.warehouse.picking.model.OrderAudit;
import com.warehouse.picking.model.User;
import com.warehouse.picking.repository.ItemRepository;
import com.warehouse.picking.service.OrderService;
import com.warehouse.picking.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/orders")
@PreAuthorize("hasRole('WAREHOUSE_ADMIN')")
public class OrderController {

    private final OrderService orderService;
    private final ItemRepository itemRepository;
    private final UserService userService;

    public OrderController(
            OrderService orderService,
            ItemRepository itemRepository,
            UserService userService) {
        this.orderService = orderService;
        this.itemRepository = itemRepository;
        this.userService = userService;
    }

    @GetMapping
    public String page(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        model.addAttribute("items", itemRepository.findAll());
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("orderForm", new OrderForm());
        return "orders";
    }

    @PostMapping
    public String create(
            @ModelAttribute OrderForm orderForm,
            RedirectAttributes redirectAttributes) {
        try {
            orderService.createOrder(
                    orderForm.getItemId(), orderForm.getQuantity());
            redirectAttributes.addFlashAttribute(
                    "message", "Order created successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/orders";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable String id,
            @RequestParam OrderStatus status,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            // Get authenticated user
            User changedBy = null;
            if (authentication != null) {
                String username = authentication.getName();
                Optional<User> userOpt = userService.findByUsername(username);
                changedBy = userOpt.orElse(null);
            }

            orderService.updateOrderStatus(id, status, changedBy);
            redirectAttributes.addFlashAttribute(
                    "message", "Order status updated.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/orders";
    }

    @GetMapping("/{id}/audit")
    public String viewAudit(
            @PathVariable String id,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            List<OrderAudit> auditTrail = orderService.getOrderAuditTrail(id);
            Optional<CustomerOrder> order = orderService.getOrderById(id);
            
            if (order.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Order not found");
                return "redirect:/orders";
            }

            model.addAttribute("order", order.get());
            model.addAttribute("auditTrail", auditTrail);
            return "order-audit";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/orders";
        }
    }

    public static class OrderForm {
        private String itemId;
        private int quantity = 1;

        public String getItemId() { return itemId; }
        public void setItemId(String itemId) { this.itemId = itemId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}
