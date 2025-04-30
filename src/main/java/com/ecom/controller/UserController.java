package com.ecom.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Cart;
import com.ecom.model.Category;
import com.ecom.model.LoyaltyPoints;
import com.ecom.model.OrderItemLimit;
import com.ecom.model.OrderRequest;
import com.ecom.model.Product;
import com.ecom.model.ProductOrder;
import com.ecom.model.UserDtls;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.LoyaltyPointsService;
import com.ecom.service.OrderItemLimitService;
import com.ecom.service.OrderNotificationService;
import com.ecom.service.OrderService;
import com.ecom.service.UserService;
import com.ecom.service.impl.OrderNotificationServiceImpl;
import com.ecom.service.impl.OrderServiceImpl;
import com.ecom.util.CommonUtil;
import com.ecom.util.OrderStatus;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserService userService;
	@Autowired
	private CategoryService categoryService;

	@Autowired
	private LoyaltyPointsService loyaltyPointService;
	
	@Autowired
	private CartService cartService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private CommonUtil commonUtil;

	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
    private OrderServiceImpl orderServiceImpl;
	
	@Autowired
    private OrderItemLimitService orderItemLimitService;

    @Autowired
    private OrderNotificationServiceImpl orderNotificationServiceimpl;
    
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    // New method to trigger order notification manually 
    @GetMapping("/user/notify-order/{orderId}")
    public String triggerOrderNotification(@PathVariable Integer orderId, HttpSession session) {
    	session.setAttribute("orderId", orderId);
        orderNotificationServiceimpl.sendOrderNotification(orderId);
        return "redirect:/user/my-orders"; // Redirect to order history after sending notification
    }

	@GetMapping("/")
	public String home() {
		return "user/base";
	}

	@GetMapping("/checkout")
	public String checkoutPage(@RequestParam Integer userId, Model model) {
		List<LoyaltyPoints> pointsList = loyaltyPointService.getPointsByUserId(userId);
		boolean isEligibleForRedemption = loyaltyPointService.isEligibleForRedemption(userId, 2000.0);
	    System.out.println("eligiblity: " + isEligibleForRedemption);
	    double totalPoints = (pointsList != null && !pointsList.isEmpty()) 
	                         ? pointsList.get(0).getTotalPoints() 
	                         : 0.0;
	    logger.info("eligible for redemption", isEligibleForRedemption);
	    model.addAttribute("totalPoints", totalPoints);
	    model.addAttribute("userId", userId);
	    model.addAttribute("isEligibleForRedemption", isEligibleForRedemption);
	    return "checkout";
	}

	@ModelAttribute
	public void getUserDetails(Principal p, Model m) {
		if (p != null) {
			String email = p.getName();
			UserDtls userDtls = userService.getUserByEmail(email);
			m.addAttribute("user", userDtls);
			Integer countCart = cartService.getCountCart(userDtls.getId());
			m.addAttribute("countCart", countCart);
		}

		List<Category> allActiveCategory = categoryService.getAllActiveCategory();
		m.addAttribute("categorys", allActiveCategory);
	}
	
	@GetMapping("/addCart")
	public String addToCart(@RequestParam Integer pid, @RequestParam Integer uid,
	        @RequestParam(defaultValue = "1") int quantity, HttpSession session, Principal principal) {
	    logger.info("Adding product to cart: productId={}, userId={}, quantity={}", pid, uid, quantity);

	    Integer userId = (Integer) session.getAttribute("uid");
	    if (userId == null && principal != null) {
	        UserDtls user = getLoggedInUserDetails(principal);
	        userId = user.getId();
	    }
	    if (userId == null) {
	        logger.warn("User not logged in, redirecting to login");
	        session.setAttribute("errorMsg", "Please log in to add items to your cart.");
	        return "redirect:/login";
	    }

	    // Fetch the current cart items for the user
	    List<Cart> userCart = cartService.getCartsByUser(userId);
	    logger.info("Cart items for user {}: {}", userId, userCart);

	    // Calculate the existing quantity of the product in the cart
	    int existingQuantity = userCart.stream()
	            .filter(cart -> {
	                Product product = cart.getProduct();
	                Integer cartProductId = (product != null) ? product.getId() : null;
	                boolean matches = cartProductId != null && cartProductId.equals(pid);
	                logger.debug("Comparing cart productId {} with request productId {}: {}", cartProductId, pid, matches);
	                return matches;
	            })
	            .mapToInt(cart -> {
	                Integer cartQuantity = cart.getQuantity();
	                int qty = cartQuantity != null ? cartQuantity : 0;
	                logger.debug("Cart item quantity for productId {}: {}", pid, qty);
	                return qty;
	            })
	            .sum();
	    logger.info("Existing quantity for productId {}: {}", pid, existingQuantity);

	    // Calculate the total quantity (existing + new)
	    int totalQuantity = existingQuantity + quantity;
	    logger.info("Total quantity for productId {}: {}", pid, totalQuantity);

	    // Check item limit
	    List<OrderItemLimit> activeLimits = orderItemLimitService.getActiveLimitByProductId(pid);
	    logger.info("Active limits for productId {}: {}", pid, activeLimits);

	    if (!activeLimits.isEmpty()) {
	        LocalDate now = LocalDate.now();
	        logger.info("Current date: {}", now);

	        boolean isWithinLimit = activeLimits.stream().anyMatch(limit -> {
	            boolean isActivePeriod = !now.isBefore(limit.getStartDate()) && !now.isAfter(limit.getEndDate());
	            boolean isQuantityWithinLimit = totalQuantity <= limit.getLimitQuantity();
	            logger.info("Checking limit: {}, isActivePeriod: {}, isQuantityWithinLimit: {}", limit, isActivePeriod, isQuantityWithinLimit);
	            return isActivePeriod && isQuantityWithinLimit;
	        });

	        if (!isWithinLimit) {
	            int maxLimit = activeLimits.stream()
	                    .mapToInt(OrderItemLimit::getLimitQuantity)
	                    .max()
	                    .orElse(0);
	            logger.info("Limit exceeded: existingQuantity={}, newQuantity={}, total={}, maxLimit={}",
	                    existingQuantity, quantity, totalQuantity, maxLimit);
	            session.setAttribute("errorMsg", "Limit reached: Only " + maxLimit + " items allowed for this product during the active period.");
	            return "redirect:/product/" + pid;
	        }
	    } else {
	        logger.info("No active limits found for productId {}. Allowing addition to cart.", pid);
	    }

	    // Save to cart
	    Cart saveCart = cartService.saveCart(pid, userId);
	    logger.info("Cart save result: {}", saveCart);
	    if (ObjectUtils.isEmpty(saveCart)) {
	        logger.error("Failed to add productId {} to cart for userId {}", pid, userId);
	        session.setAttribute("errorMsg", "Product add to cart failed");
	    } else {
	        logger.info("Successfully added productId {} to cart for userId {}", pid, userId);
	        session.setAttribute("succMsg", "Product added to cart");
	    }

	    return "redirect:/product/" + pid;
	}

	@GetMapping("/cart")
	public String loadCartPage(Principal p, Model m) {

		UserDtls user = getLoggedInUserDetails(p);
		List<Cart> carts = cartService.getCartsByUser(user.getId());
		m.addAttribute("carts", carts);
		if (carts.size() > 0) {
			Double totalOrderPrice = carts.get(carts.size() - 1).getTotalOrderPrice();
			m.addAttribute("totalOrderPrice", totalOrderPrice);
		}
		return "/user/cart";
	}

	@GetMapping("/cartQuantityUpdate")
	public String updateCartQuantity(@RequestParam String sy, @RequestParam Integer cid) {
		cartService.updateQuantity(sy, cid);
		return "redirect:/user/cart";
	}

	private UserDtls getLoggedInUserDetails(Principal p) {
		String email = p.getName();
		UserDtls userDtls = userService.getUserByEmail(email);
		return userDtls;
	}

	@GetMapping("/orders")
	public String orderPage( Principal p, Model m) {
	    UserDtls user = getLoggedInUserDetails(p);
	    List<Cart> carts = cartService.getCartsByUser(user.getId());
	    m.addAttribute("carts", carts);

	    boolean isEligibleForRedemption = loyaltyPointService.isEligibleForRedemption(user.getId(), 2000.0);
	    
	    double orderPrice = 0.0;
	    double tax = 0.0;
	    double totalOrderPrice = 0.0;
	    double totalPoints = 0.0;

	    List<LoyaltyPoints> pointsList = loyaltyPointService.getPointsByUserId(user.getId());
	    if (pointsList != null && !pointsList.isEmpty()) {
	        totalPoints = pointsList.get(0).getTotalPoints();
	    }

	    if (!carts.isEmpty()) {
	        orderPrice = carts.stream().mapToDouble(c -> c.getProduct().getDiscountPrice() * c.getQuantity()).sum();
	        tax = Math.floor((orderPrice * 0.1) * 100) / 100;
	        totalOrderPrice = orderPrice + 13.99 + tax;
	        
	        double discountPercentage = user.getDiscount() !=null ? user.getDiscount() : 0.0; // Handle null discount
	        double discountAmount = totalOrderPrice * (discountPercentage / 100);
	        double totalAfterDiscount = totalOrderPrice - discountAmount;

	        m.addAttribute("orderPrice", orderPrice);
	        m.addAttribute("tax", tax);
	        m.addAttribute("totalPoints", totalPoints);
	        m.addAttribute("totalOrderPrice", totalOrderPrice);
	        m.addAttribute("discountPercentage", discountPercentage);
	        m.addAttribute("discountAmount", discountAmount);
	        m.addAttribute("totalAfterDiscount", totalAfterDiscount);
	        m.addAttribute("isEligibleForRedemption", isEligibleForRedemption);
	        System.out.println("Controller - Total Order Price: " + totalOrderPrice);
	    }

	    return "/user/order";
	}
	@PostMapping("/save-order")
	public String saveOrder(
	        @ModelAttribute OrderRequest request,
	        @RequestParam(required = false) String redeemApplied,
	        HttpSession session,
	        Principal p,@RequestParam String firstName, @RequestParam String lastName,
            @RequestParam String email, @RequestParam String mobileNo, @RequestParam String address,
            @RequestParam String city, @RequestParam String state, @RequestParam String pincode,
            @RequestParam String paymentType) throws Exception {
	    UserDtls user = getLoggedInUserDetails(p);
	    System.out.println("Raw redeemApplied param: " + redeemApplied); // Debug raw value
	    boolean isRedeemApplied = "true".equals(redeemApplied);
	    System.out.println("Processed redeemApplied: " + isRedeemApplied);
	    orderService.saveOrder(user.getId(), request, isRedeemApplied);
	    session.removeAttribute("carts");
	    return "redirect:/user/success";
	}

	@GetMapping("/success")
	public String loadSuccess() {
		return "/user/success";
	}

	@GetMapping("/user-orders")
	public String myOrder(Model m, Principal p) {
		UserDtls loginUser = getLoggedInUserDetails(p);
		List<ProductOrder> orders = orderService.getOrdersByUser(loginUser.getId());
		m.addAttribute("orders", orders);
		return "/user/my_orders";
	}

	@GetMapping("/update-status")
	public String updateOrderStatus(@RequestParam Integer id, @RequestParam Integer st, HttpSession session) {

		OrderStatus[] values = OrderStatus.values();
		String status = null;

		for (OrderStatus orderSt : values) {
			if (orderSt.getId().equals(st)) {
				status = orderSt.getName();
			}
		}

		ProductOrder updateOrder = orderService.updateOrderStatus(id, status);
		
		try {
			commonUtil.sendMailForProductOrder(updateOrder, status);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!ObjectUtils.isEmpty(updateOrder)) {
			session.setAttribute("succMsg", "Status Updated");
		} else {
			session.setAttribute("errorMsg", "status not updated");
		}
		return "redirect:/user/user-orders";
	}

	@GetMapping("/profile")
	public String profile() {
		return "/user/profile";
	}

	@PostMapping("/update-profile")
	public String updateProfile(@ModelAttribute UserDtls user, @RequestParam MultipartFile img, HttpSession session) {
		UserDtls updateUserProfile = userService.updateUserProfile(user, img);
		if (ObjectUtils.isEmpty(updateUserProfile)) {
			session.setAttribute("errorMsg", "Profile not updated");
		} else {
			session.setAttribute("succMsg", "Profile Updated");
		}
		return "redirect:/user/profile";
	}

	@PostMapping("/change-password")
	public String changePassword(@RequestParam String newPassword, @RequestParam String currentPassword, Principal p,
			HttpSession session) {
		UserDtls loggedInUserDetails = getLoggedInUserDetails(p);

		boolean matches = passwordEncoder.matches(currentPassword, loggedInUserDetails.getPassword());

		if (matches) {
			String encodePassword = passwordEncoder.encode(newPassword);
			loggedInUserDetails.setPassword(encodePassword);
			UserDtls updateUser = userService.updateUser(loggedInUserDetails);
			if (ObjectUtils.isEmpty(updateUser)) {
				session.setAttribute("errorMsg", "Password not updated !! Error in server");
			} else {
				session.setAttribute("succMsg", "Password Updated sucessfully");
			}
		} else {
			session.setAttribute("errorMsg", "Current Password incorrect");
		}

		return "redirect:/user/profile";
	}

}
