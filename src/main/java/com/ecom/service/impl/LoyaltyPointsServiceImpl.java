package com.ecom.service.impl;

import java.sql.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecom.model.LoyaltyPoints;
import com.ecom.model.UserDtls;
import com.ecom.repository.LoyaltyPointsRepository;
import com.ecom.service.LoyaltyPointsService;


@Service
public class LoyaltyPointsServiceImpl implements LoyaltyPointsService {

    @Autowired
    private LoyaltyPointsRepository loyaltyPointsRepository;

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
}