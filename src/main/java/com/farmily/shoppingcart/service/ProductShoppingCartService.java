package com.farmily.shoppingcart.service;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.farmily.shoppingcart.dto.ShoppingcartDTO;
import com.farmily.user.security.MemberUserDetails;

public interface ProductShoppingCartService {
	List<ShoppingcartDTO> getcard(Integer userId);
	void addToCart(Integer productId,Integer quantity,Integer userId);
	void updatedQuantity(Integer productId,Integer quantity,Integer userId);
	void deleteCart(Integer userId,Integer productId);
}
