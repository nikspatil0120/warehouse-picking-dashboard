package com.warehouse.picking.service;

import com.warehouse.picking.model.Item;
import com.warehouse.picking.repository.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemService {

    private final ItemRepository repository;

    public ItemService(ItemRepository repository) {
        this.repository = repository;
    }

    public List<Item> findAll() {
        return repository.findAll()
                .stream()
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .toList();
    }

    public boolean skuExists(String sku) {
        return repository.existsBySku(sku);
    }

    public Item add(Item item) {
        return repository.save(item);
    }
}