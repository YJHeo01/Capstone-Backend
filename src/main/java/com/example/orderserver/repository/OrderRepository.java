package com.example.orderserver.repository;

import com.example.orderserver.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findAllByOrderByCreatedAtDesc();
}
