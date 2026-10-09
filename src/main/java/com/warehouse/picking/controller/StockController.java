
package com.warehouse.picking.controller;

import com.warehouse.picking.model.StockTransaction;
import com.warehouse.picking.model.StockTransaction.TransactionType;
import com.warehouse.picking.repository.ItemRepository;
import com.warehouse.picking.service.StockTransactionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/stock")
public class StockController {

    private final ItemRepository itemRepository;
    private final StockTransactionService transactionService;

    public StockController(
            ItemRepository itemRepository,
            StockTransactionService transactionService) {
        this.itemRepository = itemRepository;
        this.transactionService = transactionService;
    }

    @GetMapping
    public String page(Model model) {
        model.addAttribute("items", itemRepository.findAll());
        model.addAttribute("transactions",
                transactionService.getAllTransactions());
        model.addAttribute("transactionTypes", TransactionType.values());
        model.addAttribute("transactionForm", new TransactionForm());
        return "stock";
    }

    @PostMapping
    public String record(
            @ModelAttribute TransactionForm transactionForm,
            RedirectAttributes redirectAttributes) {
        try {
            StockTransaction transaction =
                    transactionService.recordTransaction(
                            transactionForm.getItemId(),
                            transactionForm.getType(),
                            transactionForm.getQuantity(),
                            transactionForm.getNotes());

            redirectAttributes.addFlashAttribute(
                    "message", "Stock transaction recorded successfully.");
        } catch (IllegalArgumentException | ArithmeticException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/stock";
    }

    public static class TransactionForm {
        private Long itemId;
        private TransactionType type = TransactionType.IN;
        private int quantity = 1;
        private String notes;

        public Long getItemId() { return itemId; }
        public void setItemId(Long itemId) { this.itemId = itemId; }
        public TransactionType getType() { return type; }
        public void setType(TransactionType type) { this.type = type; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
}
