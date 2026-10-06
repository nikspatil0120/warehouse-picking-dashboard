package com.warehouse.picking.controller;

import com.warehouse.picking.model.Item;
import com.warehouse.picking.repository.ItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemApiController {

    private final ItemRepository repository;

    public ItemApiController(ItemRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Item> getAllItems() {
        return repository.findAll();
    }

    @PostMapping
    public ResponseEntity<Item> createItem(@RequestBody Item item) {

        if (repository.existsBySku(item.getSku())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(repository.save(item));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItem(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}