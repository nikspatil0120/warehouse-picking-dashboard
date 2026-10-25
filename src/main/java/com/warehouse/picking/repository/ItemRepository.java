package com.warehouse.picking.repository;

import com.warehouse.picking.model.Item;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends MongoRepository<Item, String> {

    boolean existsBySku(String sku);
}
