package com.ecom.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecom.model.Cart;

import jakarta.transaction.Transactional;

public interface CartRepository extends JpaRepository<Cart, Integer> {

	public Cart findByProductIdAndUserId(Integer productId, Integer userId);

	public Integer countByUserId(Integer userId);

	@Query("SELECT c FROM Cart c WHERE c.user.id = :userId")
	public List<Cart> findByUserId(@Param("userId") Integer userId);

	    @Modifying
	    @Transactional
	    @Query(value = "INSERT INTO cart (user_id, product_id) VALUES (:userId, :productId)", nativeQuery = true)
	    void addProduct(@Param("userId") Integer userId, @Param("productId") Integer productId);
	}

