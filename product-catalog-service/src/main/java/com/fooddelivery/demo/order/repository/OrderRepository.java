package com.fooddelivery.demo.order.repository;

import com.fooddelivery.demo.order.entity.Order;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @EntityGraph(attributePaths = "items")
    Optional<Order> findWithItemsByIdAndCustomerId(
            UUID orderId,
            UUID customerId
    );

    @EntityGraph(attributePaths = "items")
    List<Order> findAllByCustomerIdOrderByCreatedAtDesc(
            UUID customerId
    );
}
