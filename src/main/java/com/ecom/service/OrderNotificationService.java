package com.ecom.service;

import org.springframework.stereotype.Service;

@Service
public interface OrderNotificationService {
	
	public void sendOrderNotification(Integer orderId);
	

}
