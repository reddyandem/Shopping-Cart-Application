package com.ecom.repository;

import com.ecom.model.OrderItemLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderItemLimitRepository extends JpaRepository<OrderItemLimit, Integer> {
    @Query("SELECT l FROM OrderItemLimit l WHERE l.productId = ?1 AND l.isActive = true")
    List<OrderItemLimit> findByProductIdAndIsActiveTrue(Integer productId);
}