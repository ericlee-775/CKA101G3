package com.farmily.shoppingcart.service;

import java.util.List;

import com.farmily.shoppingcart.dto.ShoppingcartDTO;

public interface ProductShoppingCartService {
	List<ShoppingcartDTO> getcart(Integer userId);
	void addToCart(Integer productId,Integer quantity,Integer userId);
	void updatedQuantity(Integer productId,Integer quantity,Integer userId);
	void deleteCart(Integer productId,Integer userId);
	void clearCart(Integer userId);
}
