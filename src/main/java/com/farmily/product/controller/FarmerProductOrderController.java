package com.farmily.product.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.farmily.product.model.ShippedStatus;
import com.farmily.product.service.ProductOrderService;




@RestController
@RequestMapping("/api/farmer/orders")
public class FarmerProductOrderController {
	
	@Autowired
	private ProductOrderService oSvc;
	
	
	// 更新訂單出貨狀態
	@PatchMapping("/{orderId}/shipped")
	public ResponseEntity<Void> shipped(
			@PathVariable Integer orderId,
			@RequestParam ShippedStatus status){
		// updateShippedStatus(Integer orderId, ShippedStatus status)
		oSvc.updateShippedStatus(orderId, status);
		
		return ResponseEntity.ok().build();
	}
	
	
	// 取得該小農的全部訂單
	
	
	// 取得訂單明細
	
}
