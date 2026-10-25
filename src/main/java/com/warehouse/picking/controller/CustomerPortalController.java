package com.warehouse.picking.controller;

import com.warehouse.picking.model.CustomerOrder;
import com.warehouse.picking.model.Item;
import com.warehouse.picking.model.User;
import com.warehouse.picking.repository.ItemRepository;
import com.warehouse.picking.service.OrderService;
import com.warehouse.picking.service.UserService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/customer")
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerPortalController {

    private final ItemRepository itemRepository;
    private final OrderService orderService;
    private final UserService userService;

    public CustomerPortalController(
            ItemRepository itemRepository,
            OrderService orderService,
            UserService userService) {
        this.itemRepository = itemRepository;
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping
    public String home(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        return "customer/home";
    }

    @GetMapping("/catalogue")
    public String catalogue(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "customer/catalogue";
    }

    @GetMapping("/order-form")
    public String orderForm(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        model.addAttribute("orderForm", new CustomerOrderForm());
        return "customer/order-form";
    }

    @PostMapping("/orders")
    public String createOrder(
            @ModelAttribute CustomerOrderForm form,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Get authenticated user
            String username = authentication.getName();
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "User not found");
                return "redirect:/customer/catalogue";
            }

            User customer = userOpt.get();

            // Validate form
            if (form.getItemId() == null) {
                throw new IllegalArgumentException("Item is required");
            }
            if (form.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero");
            }

            // Create order with customer association
            orderService.createOrderForCustomer(customer, form.getItemId(), form.getQuantity());
            redirectAttributes.addFlashAttribute("message", 
                    "Order created successfully");
            return "redirect:/customer/my-orders";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/customer/order-form";
        }
    }

    @GetMapping("/my-orders")
    public String myOrders(Authentication authentication, Model model) {
        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        User customer = userOpt.get();
        List<CustomerOrder> orders = orderService.getOrdersByCustomer(customer);
        
        model.addAttribute("orders", orders);
        model.addAttribute("username", username);
        
        return "customer/my-orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(
            @PathVariable String id,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        // Get authenticated user
        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User not found");
            return "redirect:/login";
        }

        User customer = userOpt.get();

        // Get order and verify ownership WITHIN THE SERVICE CALL
        Optional<CustomerOrder> orderOpt = orderService.getOrderByIdAndCustomer(id, customer);
        
        if (orderOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Order not found or you do not have access");
            return "redirect:/customer/my-orders";
        }

        CustomerOrder order = orderOpt.get();
        model.addAttribute("order", order);
        return "customer/order-detail";
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancelOrder(
            @PathVariable String id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Get authenticated user
            String username = authentication.getName();
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "User not found");
                return "redirect:/login";
            }

            User customer = userOpt.get();

            // Get order with ownership verification INSIDE the service call
            Optional<CustomerOrder> orderOpt = orderService.getOrderByIdAndCustomer(id, customer);
            
            if (orderOpt.isEmpty()) {
                throw new IllegalArgumentException("Order not found or you do not have permission to cancel it");
            }

            // Cancel order (service validates status)
            orderService.cancelOrder(id);
            
            redirectAttributes.addFlashAttribute("message", "Order cancelled successfully");
            return "redirect:/customer/my-orders";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/customer/my-orders";
        }
    }

    public static class CustomerOrderForm {
        @NotNull
        private String itemId;
        
        @Min(1)
        private int quantity = 1;

        public String getItemId() { return itemId; }
        public void setItemId(String itemId) { this.itemId = itemId; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}
