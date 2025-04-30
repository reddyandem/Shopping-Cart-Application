package com.ecom.service;

import org.springframework.beans.factory.annotation.Autowired;

import com.ecom.model.Sale;
import com.ecom.repository.SaleRespostory;

public interface SaleService {

	
	@Autowired
	 	public static final SaleRespostory saleRepository = null;

	    public default Sale saveSale(Sale sale) {
	        return saleRepository.save(sale);
	    }
}
