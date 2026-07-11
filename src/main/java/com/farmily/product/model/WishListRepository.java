package com.farmily.product.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.farmily.product.dto.ProductSummaryDTO;

public interface WishListRepository extends JpaRepository<WishListVO, WishListId> {

	@Query("SELECT new com.farmily.product.dto.ProductSummaryDTO(p.productId, p.retailPrice, p.unitPricingMeasure, p.productName) FROM WishListVO w, ProductVO p WHERE w.productId = p.productId AND w.userId = :userId")
	List<ProductSummaryDTO> findWishListByUserId(@Param("userId") Integer userId);
	
	boolean existsByProductIdAndUserId(Integer productId, Integer userId);
}
