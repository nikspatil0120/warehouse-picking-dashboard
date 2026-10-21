
package com.warehouse.picking.controller;

import com.warehouse.picking.model.CustomerOrder;
import com.warehouse.picking.model.CustomerOrder.OrderStatus;
import com.warehouse.picking.repository.ItemRepository;
import com.warehouse.picking.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final ItemRepository itemRepository;

    public OrderController(
            OrderService orderService,
            ItemRepository itemRepository) {
        this.orderService = orderService;
        this.itemRepository = itemRepository;
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
            @PathVariable Long id,
            @RequestParam OrderStatus status,
            RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(id, status);
            redirectAttributes.addFlashAttribute(
                    "message", "Order status updated.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/orders";
    }

    public static class OrderForm {
        private Long itemId;
        private int quantity = 1;

        public Long getItemId() { return itemId; }
        public void setItemId(Long itemId) { this.itemId = itemId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}
