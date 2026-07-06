package com.farmily.shoppingcart.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.farmily.shoppingcart.dto.ShoppingcartDTO;

public interface ProductShoppingCartRepository extends JpaRepository<ProductShoppingCartVO, ProductShoppingCartId>{
	 @Query("SELECT new com.farmily.shoppingcart.dto.ShoppingcartDTO("
	         + "p.productId, p.productName, p.retailPrice, c.quantity) "
	         + "FROM ProductShoppingCartVO c, ProductVO p "
	         + "WHERE c.productId = p.productId AND c.userId = :userId "
	         + "ORDER BY p.productId")
	    List<ShoppingcartDTO> findCartItems(@Param("userId") Integer userId);
	 
	 Optional<ProductShoppingCartVO> findByUserIdAndProductId(Integer userId, Integer productId);
	 
	 void deleteByUserIdAndProductId(Integer userId,Integer productId);
}
