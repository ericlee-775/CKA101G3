package com.farmily.shoppingcart.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.farmily.shoppingcart.dto.ShoppingcartDTO;
import com.farmily.shoppingcart.model.ProductShoppingCartRepository;
import com.farmily.shoppingcart.model.ProductShoppingCartVO;

import jakarta.transaction.Transactional;

@Service
public class ProductShoppingCartServiceImpl implements ProductShoppingCartService{
	
	@Autowired
	private ProductShoppingCartRepository cartRepository;
	
	public List<ShoppingcartDTO> getcard(Integer userId){
		return cartRepository.findCartItems(userId);
	}

	@Override
	@Transactional
	public void addToCart(Integer productId,Integer quantity,Integer userId) {
		ProductShoppingCartVO cartVO = new ProductShoppingCartVO();
		cartVO.setUserId(userId);
		cartVO.setQuantity(quantity);
		cartVO.setProductId(productId);
		cartRepository.save(cartVO);
		
	}

	@Override
	@Transactional
	public void updatedQuantity(Integer productId, Integer quantity, Integer userId) {
		cartRepository.findByUserIdAndProductId(userId,productId)
		.ifPresent(vo ->vo.setQuantity(quantity));
		
	}

	@Override
	@Transactional
	public void deleteCart(Integer productId,Integer userId) {
		cartRepository.deleteByUserIdAndProductId(userId,productId);		
	}}
	
	
