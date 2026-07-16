package com.farmily.product.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.farmily.product.dto.ProductSummaryDTO;

public interface WishListRepository extends JpaRepository<WishListVO, WishListId> {

	// 收藏清單：走 7 參數建構子多帶 description（5 參數位置已被 farmName 版佔用，見 DTO 註解）
	@Query("SELECT new com.farmily.product.dto.ProductSummaryDTO(p.productId, p.retailPrice, p.unitPricingMeasure, p.productName, f.farmName, p.farmerId, p.description) FROM WishListVO w, ProductVO p JOIN p.farmer f WHERE w.productId = p.productId AND w.userId = :userId")
	List<ProductSummaryDTO> findWishListByUserId(@Param("userId") Integer userId);
	
	boolean existsByProductIdAndUserId(Integer productId, Integer userId);
}
