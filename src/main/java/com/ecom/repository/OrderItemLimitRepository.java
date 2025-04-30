package com.ecom.repository;

import com.ecom.model.OrderItemLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface OrderItemLimitRepository extends JpaRepository<OrderItemLimit, Integer> {
    @Query("SELECT l FROM OrderItemLimit l WHERE l.productId = ?1 AND l.isActive = true")
    List<OrderItemLimit> findByProductIdAndIsActiveTrue(Integer productId);
    
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM OrderItemLimit o WHERE o.productId = :productId")
    boolean existsByProductId(@Param("productId") Integer productId);
    
    List<OrderItemLimit> findByProductIdAndIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Integer productId, LocalDate startDate, LocalDate endDate);
}