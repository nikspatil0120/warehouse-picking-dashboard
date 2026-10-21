
package com.warehouse.picking.service;

import com.warehouse.picking.model.Item;
import com.warehouse.picking.model.StockTransaction;
import com.warehouse.picking.model.StockTransaction.TransactionType;
import com.warehouse.picking.repository.ItemRepository;
import com.warehouse.picking.repository.StockTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StockTransactionService {

    private final ItemRepository itemRepository;
    private final StockTransactionRepository transactionRepository;

    public StockTransactionService(
            ItemRepository itemRepository,
            StockTransactionRepository transactionRepository) {
        this.itemRepository = itemRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public StockTransaction recordTransaction(
            Long itemId,
            TransactionType type,
            int quantity,
            String notes) {

        if (itemId == null) {
            throw new IllegalArgumentException("Item ID is required");
        }

        if (type == null) {
            throw new IllegalArgumentException("Transaction type is required");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Transaction quantity must be greater than zero");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Item not found with ID: " + itemId));

        if (type == TransactionType.IN) {
            item.setQuantity(Math.addExact(item.getQuantity(), quantity));
        } else {
            if (quantity > item.getQuantity()) {
                throw new IllegalArgumentException(
                        "Insufficient stock. Available quantity: "
                                + item.getQuantity());
            }

            item.setQuantity(item.getQuantity() - quantity);
        }

        itemRepository.save(item);

        StockTransaction transaction =
                new StockTransaction(item, type, quantity, notes);

        return transactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    public List<StockTransaction> getAllTransactions() {
        return transactionRepository.findAllByOrderByCreatedAtDesc();
    }
}
