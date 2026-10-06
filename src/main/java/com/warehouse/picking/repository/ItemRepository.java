package com.warehouse.picking.repository;

import com.warehouse.picking.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {

    boolean existsBySku(String sku);
}