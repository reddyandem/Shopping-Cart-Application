package com.ecom.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;


import com.ecom.model.Cart;
import com.ecom.model.Product;
import com.ecom.model.UserDtls;
import com.ecom.repository.CartRepository;
import com.ecom.repository.ProductRepository;
import com.ecom.repository.UserRepository;

import com.ecom.service.CartService;
import com.ecom.service.ProductService;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired 
    private ProductService productService;

    @Override
    public Cart saveCart(Integer productId, Integer userId) {
        UserDtls userDtls = userRepository.findById(userId).get();
        // Use ProductService to fetch the product with the effective discount applied
        Product product = productService.getProductById(productId); // Ensures sale discount is applied

        Cart cartStatus = cartRepository.findByProductIdAndUserId(productId, userId);

        Cart cart = null;

        if (ObjectUtils.isEmpty(cartStatus)) {
            cart = new Cart();
            cart.setProduct(product);
            cart.setUser(userDtls);
            cart.setQuantity(1);
            cart.setTotalPrice(1 * product.getDiscountPrice()); // Uses the effective discountPrice (sale discount if active)
        } else {
            cart = cartStatus;
            cart.setQuantity(cart.getQuantity() + 1);
            cart.setTotalPrice(cart.getQuantity() * cart.getProduct().getDiscountPrice()); // Uses the effective discountPrice
        }
        Cart saveCart = cartRepository.save(cart);

        return saveCart;
    }

    @Override
    public List<Cart> getCartsByUser(Integer userId) {
        List<Cart> carts = cartRepository.findByUserId(userId);
        Double totalOrderPrice = 0.0;
        List<Cart> updateCarts = new ArrayList<>();
        for (Cart c : carts) {
            // Ensure the product's discountPrice reflects the sale discount
            Product updatedProduct = productService.getProductById(c.getProduct().getId());
            c.setProduct(updatedProduct); // Update the product in the cart with the latest discountPrice
            Double totalPrice = (c.getProduct().getDiscountPrice() * c.getQuantity());
            c.setTotalPrice(totalPrice);
            totalOrderPrice = totalOrderPrice + totalPrice;
            c.setTotalOrderPrice(totalOrderPrice);
            updateCarts.add(c);
        }
        return updateCarts;
    }

    @Override
    public Integer getCountCart(Integer userId) {
        Integer countByUserId = cartRepository.countByUserId(userId);
        return countByUserId;
    }

    @Override
    public void updateQuantity(String sy, Integer cid) {
        Cart cart = cartRepository.findById(cid).get();
        int updateQuantity;

        if (sy.equalsIgnoreCase("de")) {
            updateQuantity = cart.getQuantity() - 1;
            if (updateQuantity <= 0) {
                cartRepository.delete(cart);
            } else {
                cart.setQuantity(updateQuantity);
                cart.setTotalPrice(updateQuantity * cart.getProduct().getDiscountPrice()); // Uses the effective discountPrice
                cartRepository.save(cart);
            }
        } else {
            updateQuantity = cart.getQuantity() + 1;
            cart.setQuantity(updateQuantity);
            cart.setTotalPrice(updateQuantity * cart.getProduct().getDiscountPrice()); // Uses the effective discountPrice
            cartRepository.save(cart);
        }
    }

    @Override
    public void addToCart(Integer userId, Integer productId) {
        Cart cart = cartRepository.findByProductIdAndUserId(productId, userId);
        if (cart == null) {
            cart = new Cart();
            cart.setUser(userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found")));
            cart.setProducts(new ArrayList<>());
        }

        Product mainProduct = productService.getProductById(productId); // Ensures sale discount is applied
        if (mainProduct.getStock() <= 0) {
            throw new RuntimeException("Main product is out of stock.");
        }

        if (!cart.getProducts().contains(mainProduct)) {
            cart.getProducts().add(mainProduct);
        }

        cartRepository.save(cart);
    }
}