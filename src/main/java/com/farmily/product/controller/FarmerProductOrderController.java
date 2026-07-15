package com.farmily.product.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.farmily.product.dto.ProductOrderFarmerResponseDTO;
import com.farmily.product.service.ProductOrderService;
import com.farmily.user.security.FarmerUserDetails;




@RestController
@RequestMapping("/api/farmer/orders")
public class FarmerProductOrderController {
	
	@Autowired
	private ProductOrderService oSvc;
	
	
	// 更新訂單出貨狀態
	@PatchMapping("/{orderId}/shipped")
	public ResponseEntity<Void> shipped(
			@AuthenticationPrincipal FarmerUserDetails me,
			@PathVariable Integer orderId){
		// updateShippedStatus(Integer orderId, ShippedStatus status)
		oSvc.updateShippedStatus(me.getFarmerId(), orderId);
		
		return ResponseEntity.ok().build();
	}
	
	
	// 顯示該小農的全部訂單
	@GetMapping
	public ResponseEntity<Page<ProductOrderFarmerResponseDTO>> getMyOrder(
			@AuthenticationPrincipal FarmerUserDetails me,
			@RequestParam (defaultValue = "0") int page){
		Page<ProductOrderFarmerResponseDTO> list = oSvc.getOrderByFarmer(me.getFarmerId(), page);
		
		return ResponseEntity.ok(list);
	}
	
	
}
