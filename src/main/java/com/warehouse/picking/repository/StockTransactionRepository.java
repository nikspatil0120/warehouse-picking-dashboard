package com.warehouse.picking.repository;

import com.warehouse.picking.model.StockTransaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockTransactionRepository extends MongoRepository<StockTransaction, String> {

    List<StockTransaction> findAllByOrderByCreatedAtDesc();
}
