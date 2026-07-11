package com.farmily.shoppingcart.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.farmily.shoppingcart.dto.CartItemRequestDTO;
import com.farmily.shoppingcart.dto.ShoppingcartDTO;
import com.farmily.shoppingcart.service.ProductShoppingCartService;
import com.farmily.user.security.MemberUserDetails;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/shoppingcart")
public class ProductShoppingCartController {
	
	@Autowired
	private ProductShoppingCartService cartService;
	
	@GetMapping
	public ResponseEntity<List<ShoppingcartDTO>> getCart(@AuthenticationPrincipal MemberUserDetails me){
		return ResponseEntity.ok(cartService.getcart(me.getUserId()));
	}
	@PostMapping("/{productId}")
	public ResponseEntity<Void> insertCart(@PathVariable Integer productId,
										  @Valid @RequestBody CartItemRequestDTO req,
										  @AuthenticationPrincipal MemberUserDetails me){
		cartService.addToCart(productId,req.getQuantity(),me.getUserId());
		return ResponseEntity.ok().build();
	}
	@PutMapping("/{productId}")
	public ResponseEntity<Void> updatedQuantity(@PathVariable Integer productId,
			  									@Valid @RequestBody CartItemRequestDTO req,
			  									@AuthenticationPrincipal MemberUserDetails me){
		cartService.updatedQuantity(productId,req.getQuantity(),me.getUserId());
		return ResponseEntity.ok().build();
	}
	@DeleteMapping("/{productId}")
	public ResponseEntity<Void> deleteCart(@PathVariable Integer productId,
										  @AuthenticationPrincipal MemberUserDetails me){
		cartService.deleteCart(productId,me.getUserId());
		return ResponseEntity.ok().build();
	}
	//當思媛姐姐訂單成立可以清空購物車
	@DeleteMapping
	public ResponseEntity<Void> clearCart(@AuthenticationPrincipal MemberUserDetails me){
		cartService.clearCart(me.getUserId());
		return ResponseEntity.ok().build();
	}
	
	
}
