
package com.warehouse.picking.controller;

import com.warehouse.picking.model.Item;
import com.warehouse.picking.repository.ItemRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertApiController {

    private final ItemRepository itemRepository;

    public AlertApiController(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @GetMapping
    public List<Map<String, Object>> getStockAlerts() {
        return itemRepository.findAll().stream()
                .filter(item -> item.getQuantity() == 0
                        || item.getQuantity() <= item.getReorderThreshold())
                .map(item -> Map.<String, Object>of(
                        "itemId", item.getId(),
                        "sku", item.getSku(),
                        "name", item.getName(),
                        "quantity", item.getQuantity(),
                        "reorderThreshold", item.getReorderThreshold(),
                        "alertType", item.getQuantity() == 0
                                ? "OUT_OF_STOCK"
                                : "LOW_STOCK"
                ))
                .toList();
    }
}
