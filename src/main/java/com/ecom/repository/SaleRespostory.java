package com.ecom.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecom.model.Sale;

public interface SaleRespostory extends JpaRepository<Sale, Long> {

}
