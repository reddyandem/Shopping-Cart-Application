package com.ecom.service.impl;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecom.model.Cart;
import com.ecom.model.Product;
import com.ecom.model.ProductOrder;
import com.ecom.model.UserDtls;
import com.ecom.repository.CartRepository;
import com.ecom.repository.ProductOrderRepository;
import com.ecom.repository.ProductRepository;
import com.ecom.repository.UserRepository;
import com.ecom.util.CommonUtil;

@Service
public class OrderNotificationServiceImpl {

    @Autowired
    private ProductOrderRepository productOrderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommonUtil commonUtil;

    public void sendOrderNotification(Integer orderId) {
    	// Fetch the order
        ProductOrder order = productOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Fetch user details
        UserDtls user = userRepository.findById(order.getId())
                .orElseThrow(() -> new RuntimeException("User not found for order: " + orderId));

        // Fetch carts associated with the order (assuming carts are linked to the order)
        List<Cart> carts = cartRepository.findByUserId(user.getId());

        // Calculate order details
        double totalAmount = order.getPrice() + 13.99 +(order.getPrice() * 0.1) ; // Price + delivery fee + tax
        double saleSavings = calculateSaleSavings(carts);
        double redemptionSavings = order.getPointsRedeemed() != null ? (order.getPointsRedeemed() / 100.0) * 5.0 : 0.0; // Assuming 100 points = $5

        // Build email content as a single HTML string, including the subject in the body
        String message = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>Order Confirmation</title>"
                + "<style>body { font-family: Arial, sans-serif; margin: 20px; } h1, h2, h3 { color: #333; } ul { list-style-type: none; padding: 0; } li { margin: 10px 0; } .highlight { color: #006400; font-weight: bold; }</style></head>"
                + "<body>"
                + "<h1>Order Confirmation - Order #" + order.getOrderId() + "</h1>"
                + "<h2>Dear " + user.getName() + ",</h2>"
                + "<p>Your order (Order #" + order.getOrderId() + ") has been placed successfully on "
                + order.getOrderDate().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")) + "!</p>"
                + "<h3>Order Details:</h3>"
                + "<ul>";

        for (Cart cart : carts) {
            Product product = cart.getProduct();
            double originalPrice = product.getPrice();
            double discountedPrice = product.getDiscountPrice();
            double productSaleSavings = originalPrice - discountedPrice;
            message += "<li>"
                    + product.getTitle() + " (Qty: " + cart.getQuantity() + ")"
                    + " - Original Price: $" + String.format("%.2f", originalPrice)
                    + ", Discounted Price: $" + String.format("%.2f", discountedPrice)
                    + ", Saved: $" + String.format("%.2f", productSaleSavings) + "</li>";
        }

        message += "</ul>"
                + "<p><strong>Total Amount:</strong> <span class=\"highlight\">$" + String.format("%.2f", totalAmount) + "</span></p>"
                + "<p><strong>Saved through Sale:</strong> <span class=\"highlight\">$" + String.format("%.2f", saleSavings) + "</span></p>"
                + "<p><strong>Saved through Redemption:</strong> <span class=\"highlight\">$" + String.format("%.2f", redemptionSavings) + "</span></p>"
                + "<p>Track your order or view order history at: <a href=\"http://localhost:8080/user/my-orders\">My Orders</a></p>"
                + "<p>Thank you for shopping with us!<br>E-Commerce Team</p>"
                + "</body></html>";

        // Send email using the existing sendMail method with two parameters
        try {
        commonUtil.sendMail(user.getEmail(), message);
    }
        catch(Exception e) 
        {
        	System.out.println("error sendingemail");
        	}
        }

    private double calculateSaleSavings(List<Cart> carts) {
        return carts.stream()
                .mapToDouble(cart -> {
                    Product product = cart.getProduct();
                    double originalPrice = product.getPrice();
                    double discountedPrice = product.getDiscountPrice();
                    return originalPrice - discountedPrice;
                })
                .sum();
    }
}