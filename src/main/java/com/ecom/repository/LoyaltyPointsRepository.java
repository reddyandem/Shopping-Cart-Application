package com.ecom.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecom.model.LoyaltyPoints;

public interface LoyaltyPointsRepository extends JpaRepository<LoyaltyPoints, Integer> {
	
	List<LoyaltyPoints> findByUserId(Integer userId);
	
	

}
