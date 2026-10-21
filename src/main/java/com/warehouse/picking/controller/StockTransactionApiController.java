
package com.warehouse.picking.controller;

import com.warehouse.picking.model.StockTransaction;
import com.warehouse.picking.model.StockTransaction.TransactionType;
import com.warehouse.picking.service.StockTransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@Validated
public class StockTransactionApiController {

    private final StockTransactionService stockTransactionService;

    public StockTransactionApiController(
            StockTransactionService stockTransactionService) {
        this.stockTransactionService = stockTransactionService;
    }

    @GetMapping
    public List<StockTransaction> getTransactions() {
        return stockTransactionService.getAllTransactions();
    }

    @PostMapping
    public ResponseEntity<?> createTransaction(
            @Valid @RequestBody TransactionRequest request) {
        try {
            StockTransaction transaction =
                    stockTransactionService.recordTransaction(
                            request.itemId(),
                            request.type(),
                            request.quantity(),
                            request.notes());

            return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
        } catch (IllegalArgumentException | ArithmeticException exception) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", exception.getMessage()));
        }
    }

    public record TransactionRequest(
            @NotNull(message = "Item ID is required")
            Long itemId,

            @NotNull(message = "Transaction type is required")
            TransactionType type,

            @Min(value = 1, message = "Quantity must be at least 1")
            int quantity,

            String notes) {
    }
}
