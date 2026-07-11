package com.farmily.product.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.farmily.product.dto.ProductSummaryDTO;
import com.farmily.product.service.ProductService;
import com.farmily.user.security.MemberUserDetails;

@RestController
@RequestMapping("/api/member/wishlists")
public class MemberWishlistController {

	@Autowired
	private ProductService productService;
	
	//新增收藏
	@PostMapping("/{productId}")
	public ResponseEntity<Void> addWishList(@PathVariable Integer productId,
			@AuthenticationPrincipal MemberUserDetails me) {

		boolean added = productService.addWishList(productId, me.getUserId());
		return added ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();

	}
	//刪除收藏
	@DeleteMapping("/{productId}")
	public ResponseEntity<Void> deleteWishList(@PathVariable Integer productId,
			@AuthenticationPrincipal MemberUserDetails me) {

		boolean deleted = productService.deleteWishList(productId, me.getUserId());
		return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();

	}
	//查詢全部收藏
	@GetMapping
	public ResponseEntity<List<ProductSummaryDTO>> getWishLists(@AuthenticationPrincipal MemberUserDetails me) {

		List<ProductSummaryDTO> wishlist = productService.getAllWishLists(me.getUserId());
		return ResponseEntity.ok(wishlist);
	}

	
	
}
