
package com.warehouse.picking.controller;

import com.warehouse.picking.model.Item;
import com.warehouse.picking.repository.ItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Locale;

@RestController
@RequestMapping("/api")
public class ItemApiController {

    private final ItemRepository repository;

    public ItemApiController(ItemRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/items")
    public List<Item> getAllItems(
            @RequestParam(required = false) String search) {

        List<Item> items = repository.findAll();

        if (search == null || search.isBlank()) {
            return items;
        }

        String query = search.toLowerCase(Locale.ROOT);

        return items.stream()
                .filter(item ->
                        (item.getSku() != null
                                && item.getSku().toLowerCase(Locale.ROOT)
                                .contains(query))
                        || (item.getName() != null
                                && item.getName().toLowerCase(Locale.ROOT)
                                .contains(query)))
                .toList();
    }

    @PostMapping("/items")
    public ResponseEntity<Item> createItem(@RequestBody Item item) {

        if (repository.existsBySku(item.getSku())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(repository.save(item));
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<Item> getItem(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stock")
    public List<Map<String, Object>> getStockStatus() {
        return repository.findAll().stream()
                .map(item -> {
                    String status;

                    if (item.getQuantity() == 0) {
                        status = "OUT_OF_STOCK";
                    } else if (item.getQuantity()
                            <= item.getReorderThreshold()) {
                        status = "LOW_STOCK";
                    } else {
                        status = "IN_STOCK";
                    }

                    return Map.<String, Object>of(
                            "itemId", item.getId(),
                            "sku", item.getSku(),
                            "name", item.getName(),
                            "quantity", item.getQuantity(),
                            "reorderThreshold", item.getReorderThreshold(),
                            "status", status
                    );
                })
                .toList();
    }
}
