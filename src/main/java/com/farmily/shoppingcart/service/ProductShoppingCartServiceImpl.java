package com.farmily.shoppingcart.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.farmily.product.model.ProductRepository;
import com.farmily.shoppingcart.dto.ShoppingcartDTO;
import com.farmily.shoppingcart.model.ProductShoppingCartRepository;
import com.farmily.shoppingcart.model.ProductShoppingCartVO;

import jakarta.transaction.Transactional;

@Service
public class ProductShoppingCartServiceImpl implements ProductShoppingCartService{
	
	@Autowired
	private ProductShoppingCartRepository cartRepository;
	
	@Autowired 
	private ProductRepository productRepository;
	
	public List<ShoppingcartDTO> getcart(Integer userId){
		return cartRepository.findCartItems(userId);
	}

	@Override
	@Transactional
	public void addToCart(Integer productId,Integer quantity,Integer userId) {
		if(!productRepository.existsById(productId)) {
			throw new IllegalArgumentException("商品不存在");
		}
		cartRepository.findByUserIdAndProductId(userId, productId)
			.ifPresentOrElse(
				vo -> vo.setQuantity(vo.getQuantity() + quantity),
				() -> {
					ProductShoppingCartVO cartVO = new ProductShoppingCartVO();
					cartVO.setUserId(userId);
					cartVO.setQuantity(quantity);
					cartVO.setProductId(productId);
					cartRepository.save(cartVO);
				});
	}

	@Override
	@Transactional
	public void updatedQuantity(Integer productId, Integer quantity, Integer userId) {
		cartRepository.findByUserIdAndProductId(userId,productId)
		.orElseThrow(()->new IllegalArgumentException("購物車中無此商品"))
		.setQuantity(quantity);
	}

	@Override
	@Transactional
	public void deleteCart(Integer productId,Integer userId) {
		cartRepository.deleteByUserIdAndProductId(userId,productId);		
	}
	//當訂單成立清空購物車
	@Override
	@Transactional
	public void clearCart(Integer userId) {
		cartRepository.deleteByUserId(userId);
	}}

	
	
	
