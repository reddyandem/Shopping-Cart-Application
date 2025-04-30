package com.ecom.service.impl;

import java.sql.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecom.controller.AdminController;
import com.ecom.model.LoyaltyPoints;
import com.ecom.model.UserDtls;
import com.ecom.repository.LoyaltyPointsRepository;
import com.ecom.repository.ProductOrderRepository;
import com.ecom.service.LoyaltyPointsService;


@Service
public class LoyaltyPointsServiceImpl implements LoyaltyPointsService {

    @Autowired
    private LoyaltyPointsRepository loyaltyPointsRepository;
    
    @Autowired 
    private ProductOrderRepository productOrderRepository;

    @Override
    public List<LoyaltyPoints> getPointsByUserId(Integer userId) {
        return loyaltyPointsRepository.findByUserId(userId);
    }

    @Override
    public void addPoints(UserDtls user, Double amountSpent) {
        List<LoyaltyPoints> pointsList = loyaltyPointsRepository.findByUserId(user.getId());
        LoyaltyPoints points;
        double pointsToAdd = amountSpent * 1.5;

        if (pointsList.isEmpty()) {
            points = new LoyaltyPoints();
            points.setUser(user);
            points.setTotalPoints(pointsToAdd);
        } else {
            points = pointsList.get(0);
            points.setTotalPoints(points.getTotalPoints() + pointsToAdd);
        }
        points.setLastUpdated(new Date(0));
        loyaltyPointsRepository.save(points);
        System.out.println("Adding points: " + pointsToAdd + ", New Total: " + points.getTotalPoints());
    }

    @Override
    public boolean redeemPoints(UserDtls user, Double pointsToRedeem) {
        LoyaltyPoints loyaltyPoints = user.getLoyaltyPoints();
        if (loyaltyPoints == null || loyaltyPoints.getAvailablePoints() < pointsToRedeem) {
            return false;
        }
        loyaltyPoints.setRedeemedPoints(loyaltyPoints.getRedeemedPoints() + pointsToRedeem);
        loyaltyPoints.setLastUpdated(new Date(0));
        loyaltyPointsRepository.save(loyaltyPoints);
        return true;
    }

    @Override
    public void deductPoints(Integer userId, double pointsToDeduct) {
        List<LoyaltyPoints> pointsList = getPointsByUserId(userId);
        if (pointsList.isEmpty()) {
            return;
        }
        LoyaltyPoints points = pointsList.get(0);
        if (points.getTotalPoints() >= pointsToDeduct) {
            points.setTotalPoints(points.getTotalPoints() - pointsToDeduct);
            points.setRedeemedPoints(points.getRedeemedPoints() + pointsToDeduct);
            points.setLastUpdated(new Date(userId));
            loyaltyPointsRepository.save(points);
            System.out.println("Points deducted: " + pointsToDeduct + ", New Total: " + points.getTotalPoints());
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    
	@Override
	public boolean isEligibleForRedemption(Integer userId, double minimumPoints) { 
logger.info("Checking redemption eligibility for userId: {}, minimumPoints: {}", userId, minimumPoints);
        
        // Fetch loyalty points
        List<LoyaltyPoints> pointsList = getPointsByUserId(userId);
        double totalPoints = (pointsList != null && !pointsList.isEmpty()) ? pointsList.get(0).getTotalPoints() : 0.0;
        logger.info("Total points for userId {}: {}", userId, totalPoints);
        
        // Count past orders
        long orderCount = productOrderRepository.countByUserId(userId);
        System.out.println("Order count for userId" + userId +  orderCount);
        
        // Check eligibility
        boolean eligible = totalPoints >= minimumPoints && orderCount > 5;
        logger.info("Eligibility check for userId {}: totalPoints={} (>= {}), orderCount={} (> 5), eligible={}",
                userId, totalPoints, minimumPoints, orderCount, eligible);
        return eligible;
    }
}