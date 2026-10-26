
package com.warehouse.picking.controller;

import com.warehouse.picking.model.StockTransaction;
import com.warehouse.picking.model.StockTransaction.TransactionType;
import com.warehouse.picking.repository.ItemRepository;
import com.warehouse.picking.service.StockTransactionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/stock")
@PreAuthorize("hasRole('WAREHOUSE_ADMIN')")
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
        // Only show IN as the transaction type (stock-out is automatic via orders)
        model.addAttribute("transactionTypes", new TransactionType[]{TransactionType.IN});
        model.addAttribute("transactionForm", new TransactionForm());
        return "stock";
    }

    @PostMapping
    public String record(
            @ModelAttribute TransactionForm transactionForm,
            RedirectAttributes redirectAttributes) {
        try {
            // Force type to IN (stock-out is handled automatically by orders)
            transactionForm.setType(TransactionType.IN);
            
            StockTransaction transaction =
                    transactionService.recordTransaction(
                            transactionForm.getItemId(),
                            transactionForm.getType(),
                            transactionForm.getQuantity(),
                            transactionForm.getNotes());

            redirectAttributes.addFlashAttribute(
                    "message", "Stock receipt recorded successfully.");
        } catch (IllegalArgumentException | ArithmeticException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/stock";
    }

    public static class TransactionForm {
        private String itemId;
        private TransactionType type = TransactionType.IN;
        private int quantity = 1;
        private String notes;

        public String getItemId() { return itemId; }
        public void setItemId(String itemId) { this.itemId = itemId; }
        public TransactionType getType() { return type; }
        public void setType(TransactionType type) { this.type = type; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
}
