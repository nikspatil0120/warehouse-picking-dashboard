
package com.warehouse.picking.repository;

import com.warehouse.picking.model.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockTransactionRepository
        extends JpaRepository<StockTransaction, Long> {

    List<StockTransaction> findAllByOrderByCreatedAtDesc();
}
