package com.ecom.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import com.ecom.model.Cart;
import com.ecom.model.LoyaltyPoints;
import com.ecom.model.OrderAddress;
import com.ecom.model.OrderRequest;
import com.ecom.model.ProductOrder;
import com.ecom.model.UserDtls;
import com.ecom.repository.CartRepository;
import com.ecom.repository.ProductOrderRepository;
import com.ecom.service.LoyaltyPointsService;
import com.ecom.service.OrderService;
import com.ecom.service.UserService;
import com.ecom.util.CommonUtil;
import com.ecom.util.OrderStatus;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

	@Autowired
	private ProductOrderRepository orderRepository;
	

	@Autowired
	private LoyaltyPointsService loyaltyPointService;
	
	@Autowired
	private UserService userService;

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private CommonUtil commonUtil;

	@Override
	public void saveOrder(Integer userId, OrderRequest orderRequest, boolean redeemApplied) throws Exception {
	    try {
	    	ProductOrder order = new ProductOrder();
	        UserDtls user = userService.getUserById(userId);
	        List<Cart> carts = cartRepository.findByUserId(userId);
	        if (carts.isEmpty()) {
	            throw new Exception("Cart is empty");
	        }

	        double orderPrice = carts.stream()
	            .mapToDouble(cart -> cart.getProduct().getDiscountPrice() * cart.getQuantity())
	            .sum();
	        double tax = Math.floor((orderPrice * 0.1) * 100) / 100;
	        double totalPrice = orderPrice + 13.99 + tax;
	        
	        double discountPercentage = user.getDiscount(); // e.g., 10.0 for 10%
	        double discountAmount = totalPrice * (discountPercentage / 100);
	        double totalAfterDiscount = totalPrice - discountAmount;
	     // Round to two decimal places
	        BigDecimal roundedTotal = BigDecimal.valueOf(totalAfterDiscount)
	                .setScale(2, RoundingMode.HALF_UP);
	        order.setPrice(roundedTotal.doubleValue());

	        double redeemableAmount = 0.0;
	        double pointsToDeduct = 0.0;

	        if (redeemApplied) {
	            List<LoyaltyPoints> pointsList = loyaltyPointService.getPointsByUserId(userId);
	            double totalPoints = pointsList.isEmpty() ? 0.0 : pointsList.get(0).getTotalPoints();

	            if (totalPoints >= 2000) {
	                redeemableAmount = Math.min(totalPoints * 0.1, totalPrice);
	                pointsToDeduct = redeemableAmount / 0.1;
	                totalPrice -= redeemableAmount;
	                totalPrice = Math.max(0, totalPrice);
	                loyaltyPointService.deductPoints(userId, pointsToDeduct);
	            } else {
	                redeemApplied = false;
	            }
	        }

	        System.out.println("Cart Value: " + orderPrice + ", Tax: " + tax + ", Total Before Redemption: " + (orderPrice + 13.99 + tax));
	        System.out.println("Redeem Applied: " + redeemApplied + ", Redeemable Amount: " + redeemableAmount + 
	            ", Points Deducted: " + pointsToDeduct + ", Final Total: " + totalPrice);

	        for (Cart cart : carts) {
	            //ProductOrder order = new ProductOrder();
	            order.setOrderId(UUID.randomUUID().toString());
	            order.setOrderDate(LocalDate.now());
	            order.setProduct(cart.getProduct());
	            order.setPrice(totalPrice / carts.size());
	            order.setQuantity(cart.getQuantity());
	            order.setUser(cart.getUser());
	            order.setStatus(OrderStatus.IN_PROGRESS.getName());
	            order.setPaymentType(orderRequest.getPaymentType());

	            OrderAddress address = new OrderAddress();
	            address.setFirstName(orderRequest.getFirstName());
	            address.setLastName(orderRequest.getLastName());
	            address.setEmail(orderRequest.getEmail());
	            address.setMobileNo(orderRequest.getMobileNo());
	            address.setAddress(orderRequest.getAddress());
	            address.setCity(orderRequest.getCity());
	            address.setState(orderRequest.getState());
	            address.setPincode(orderRequest.getPincode());
	            order.setOrderAddress(address);

	            if (redeemApplied && pointsToDeduct > 0) {
	                order.setPointsRedeemed((int) (pointsToDeduct / carts.size()));
	            }

	            int pointsEarned = (int) (totalPrice * 1.5 / carts.size());
	            order.setPointsEarned(pointsEarned);

	            ProductOrder saveOrder = orderRepository.save(order);
	            
	            commonUtil.sendMailForProductOrder(saveOrder, null);
	        }

	        if (totalPrice > 0) {
	            double pointsToAdd = totalPrice * 1.5;
	            loyaltyPointService.addPoints(user, totalPrice);
	            System.out.println("Points added for purchase: " + pointsToAdd);
	        } else {
	            System.out.println("No points added: Final total is zero");
	        }

	        cartRepository.deleteAll(carts);
	    } catch (Exception e) {
	        System.err.println("Error in saveOrder: " + e.getMessage());
	        e.printStackTrace();
	        throw e;
	    }
	}
	@Override
	public List<ProductOrder> getOrdersByUser(Integer userId) {
		List<ProductOrder> orders = orderRepository.findByUserId(userId);
		return orders;
	}

	@Override
	public ProductOrder updateOrderStatus(Integer id, String status) {
		Optional<ProductOrder> findById = orderRepository.findById(id);
		if (findById.isPresent()) {
			ProductOrder productOrder = findById.get();
			productOrder.setStatus(status);
			ProductOrder updateOrder = orderRepository.save(productOrder);
			return updateOrder;
		}
		return null;
	}

	@Override
	public List<ProductOrder> getAllOrders() {
		return orderRepository.findAll();
	}

	@Override
	public Page<ProductOrder> getAllOrdersPagination(Integer pageNo, Integer pageSize) {
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		return orderRepository.findAll(pageable);

	}

	@Override
	public ProductOrder getOrdersByOrderId(String orderId) {
		return orderRepository.findByOrderId(orderId);
	}

}
