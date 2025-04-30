package com.ecom.service;

import com.ecom.model.OrderItemLimit;
import com.ecom.repository.OrderItemLimitRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderItemLimitService {
	private static final Logger logger = LoggerFactory.getLogger(OrderItemLimitService.class);
    @Autowired
    private OrderItemLimitRepository orderItemLimitRepository;

    public OrderItemLimit saveOrderItemLimit(OrderItemLimit limit) {
    	
        return orderItemLimitRepository.save(limit);
    }

    public List<OrderItemLimit> getAllActiveLimits() {
        LocalDate now = LocalDate.now();
        return orderItemLimitRepository.findAll().stream()
                .filter(limit -> limit.isActive() && !now.isBefore(limit.getStartDate()) && !now.isAfter(limit.getEndDate()))
                .collect(Collectors.toList());
    }

    public List<OrderItemLimit> getActiveLimitByProductId(Integer productId) {
        logger.info("Fetching active limits for productId: {}", productId);
        LocalDate now = LocalDate.now();
        List<OrderItemLimit> limits = orderItemLimitRepository.findByProductIdAndIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            productId, now, now);
        logger.info("Found {} active limits for productId {}: {}", limits.size(), productId, limits);
        return limits;
    }
    

    public boolean isWithinLimit(Integer productId, int quantity) {
        LocalDate now = LocalDate.now();
        List<OrderItemLimit> activeLimits = getActiveLimitByProductId(productId);
        if (activeLimits.isEmpty()) {
            return true; // No limit if no active rule exists
        }
        return activeLimits.stream().anyMatch(limit ->
                now.isAfter(limit.getStartDate()) && !now.isAfter(limit.getEndDate()) && quantity <= limit.getLimitQuantity());
    }
    
    
    public boolean existsByProductId(Integer productId) {
    	logger.info("Checking if order item limit exists for productId: {}", productId);
        boolean exists = orderItemLimitRepository.existsByProductId(productId);
        logger.info("Result for productId {}: {}", productId, exists);
        return exists;
    }
    
    }
