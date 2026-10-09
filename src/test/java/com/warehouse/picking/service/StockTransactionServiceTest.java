
package com.warehouse.picking.service;

import com.warehouse.picking.model.Item;
import com.warehouse.picking.model.StockTransaction;
import com.warehouse.picking.repository.ItemRepository;
import com.warehouse.picking.repository.StockTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockTransactionServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private StockTransactionRepository transactionRepository;

    @InjectMocks
    private StockTransactionService stockTransactionService;

    private Item item;

    @BeforeEach
    void setUp() {
        item = new Item("SKU-TEST", "Test Item", "A1", 10, 3);
        item.setId(1L);
    }

    @Test
    void stockInShouldIncreaseQuantity() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(transactionRepository.save(any(StockTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        stockTransactionService.recordTransaction(
                1L, StockTransaction.TransactionType.IN, 5, "Restock");

        assertEquals(15, item.getQuantity());
        verify(itemRepository).save(item);
        verify(transactionRepository).save(any(StockTransaction.class));
    }

    @Test
    void stockOutShouldDecreaseQuantity() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(transactionRepository.save(any(StockTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        stockTransactionService.recordTransaction(
                1L, StockTransaction.TransactionType.OUT, 4, "Picking");

        assertEquals(6, item.getQuantity());
        verify(itemRepository).save(item);
        verify(transactionRepository).save(any(StockTransaction.class));
    }

    @Test
    void stockOutShouldRejectInsufficientQuantity() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(IllegalArgumentException.class, () ->
                stockTransactionService.recordTransaction(
                        1L, StockTransaction.TransactionType.OUT, 11, "Picking"));

        assertEquals(10, item.getQuantity());
        verify(itemRepository, never()).save(any(Item.class));
        verify(transactionRepository, never()).save(any(StockTransaction.class));
    }

    @Test
    void transactionShouldRejectZeroQuantity() {
        assertThrows(IllegalArgumentException.class, () ->
                stockTransactionService.recordTransaction(
                        1L, StockTransaction.TransactionType.IN, 0, "Invalid"));

        verifyNoInteractions(itemRepository, transactionRepository);
    }
}
