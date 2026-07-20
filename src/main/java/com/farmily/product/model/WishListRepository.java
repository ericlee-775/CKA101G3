package com.farmily.product.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.farmily.product.dto.ProductSummaryDTO;

public interface WishListRepository extends JpaRepository<WishListVO, WishListId> {

	// 收藏清單：走 7 參數建構子多帶 description（5 參數位置已被 farmName 版佔用，見 DTO 註解）
	// 加 status = ACTIVE + farmerStatus = ACTIVE，跟其他買家端查詢（findDetailById 等）標準一致：
	// 商品下架/小農停權後，不再出現在收藏清單裡，避免點進去撞 404
	@Query("SELECT new com.farmily.product.dto.ProductSummaryDTO(p.productId, p.retailPrice, p.unitPricingMeasure, p.productName, f.farmName, p.farmerId, p.description) "
			+ "FROM WishListVO w, ProductVO p JOIN p.farmer f "
			+ "WHERE w.productId = p.productId AND w.userId = :userId "
			+ "AND p.status = com.farmily.product.model.ProductStatus.ACTIVE "
			+ "AND f.farmerStatus = ACTIVE")
	List<ProductSummaryDTO> findWishListByUserId(@Param("userId") Integer userId);
	
	boolean existsByProductIdAndUserId(Integer productId, Integer userId);
}
