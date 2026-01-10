package org.example.megasegashop.order.repository;

import org.example.megasegashop.order.entity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    
    @Override
    @EntityGraph(attributePaths = "items")
    Optional<Order> findById(Long id);
}
