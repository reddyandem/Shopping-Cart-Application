package com.ecom.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ecom.model.LoyaltyPoints;
import com.ecom.model.UserDtls;

@Service
public interface LoyaltyPointsService {
    List<LoyaltyPoints> getPointsByUserId(Integer userId);
    void addPoints(UserDtls user, Double amountSpent);
    boolean redeemPoints(UserDtls user, Double pointsToRedeem);
    void deductPoints(Integer userId, double pointsToDeduct); 
}
