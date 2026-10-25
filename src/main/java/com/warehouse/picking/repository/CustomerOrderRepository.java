package com.warehouse.picking.repository;

import com.warehouse.picking.model.CustomerOrder;
import com.warehouse.picking.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerOrderRepository extends MongoRepository<CustomerOrder, String> {

    List<CustomerOrder> findAllByOrderByCreatedAtDesc();

    List<CustomerOrder> findAllByCustomerOrderByCreatedAtDesc(User customer);

    Optional<CustomerOrder> findByIdAndCustomer(String id, User customer);
}
