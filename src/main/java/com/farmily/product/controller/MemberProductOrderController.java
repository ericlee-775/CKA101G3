package com.farmily.product.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.farmily.product.dto.ProductOrderCheckoutInfoDTO;
import com.farmily.product.dto.ProductOrderItemResponseDTO;
import com.farmily.product.dto.ProductOrderRequestDTO;
import com.farmily.product.dto.ProductOrderResponseDTO;
import com.farmily.product.model.ShippedStatus;
import com.farmily.product.service.ProductOrderService;
import com.farmily.user.security.MemberUserDetails;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/member/orders")
public class MemberProductOrderController {
	
	@Autowired
	private ProductOrderService oSvc;
	
	// 訂單資料確認 (取得預設的消費者資料預填訂單頁: 地址)
	@GetMapping("/checkout-info")
	public ResponseEntity<ProductOrderCheckoutInfoDTO> checkOutInfo(@AuthenticationPrincipal MemberUserDetails me){
		ProductOrderCheckoutInfoDTO dto = oSvc.getCheckoutInfo(me.getUser());
		return ResponseEntity.ok(dto);
	}
	
	
	// 購物車結帳
	@PostMapping("/checkout")
	public ResponseEntity<Void> checkout(
			@AuthenticationPrincipal MemberUserDetails me,
			@Valid @RequestBody ProductOrderRequestDTO req) {
		
		oSvc.addOrder(req, me.getUserId());
		
		return ResponseEntity.ok().build();
	}
	
	
	// 確認收貨
	@PatchMapping("/{orderId}/received")
	public ResponseEntity<Void> received(
			@AuthenticationPrincipal MemberUserDetails me,
			@PathVariable Integer orderId){
		// updateReceived(Integer orderId)
		oSvc.updateReceived(me.getUserId(), orderId);
		return ResponseEntity.ok().build();
	}
	
	// 顯示訂單列表
	@GetMapping
	public ResponseEntity<Page<ProductOrderResponseDTO>> getMyOrder(
			@AuthenticationPrincipal MemberUserDetails me,
			@RequestParam (defaultValue = "0") int page){
		// Page<ProductOrderResponseDTO> getOrderByUser(Integer userId, int page)
		Page<ProductOrderResponseDTO> list = oSvc.getOrderByUser(me.getUserId(), page);
		
		return ResponseEntity.ok(list);
	}
	
	// 顯示訂單明細
	@GetMapping("/{orderId}/items")
	public ResponseEntity<Page<ProductOrderItemResponseDTO>> getOrderItems(
			@AuthenticationPrincipal MemberUserDetails me,
			@PathVariable Integer orderId,
			@RequestParam (defaultValue = "0") int page){
		// Page<ProductOrderItemResponseDTO> getOrderItem(Integer userId, Integer orderId, int page)
		Page<ProductOrderItemResponseDTO> list = oSvc.getOrderItem(me.getUserId(), orderId, page);
		
		return ResponseEntity.ok(list);
	}

}
