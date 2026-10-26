package com.warehouse.picking.repository;

import com.warehouse.picking.model.OrderAudit;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderAuditRepository extends MongoRepository<OrderAudit, String> {
    List<OrderAudit> findByOrderIdOrderByChangedAtDesc(String orderId);
}
