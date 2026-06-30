package com.farmily.product.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.farmily.product.dto.ProductDetailDTO;
import com.farmily.product.dto.ProductGroupBuyDTO;
import com.farmily.product.dto.ProductSummeryDTO;

public interface ProductRepository extends JpaRepository<ProductVO, Integer>{
	@Query("SELECT new com.farmily.product.dto.ProductSummeryDTO(p.productId, p.retailPrice, p.unitPricingMeasure, p.productName) FROM ProductVO p")
	List<ProductSummeryDTO> findAllProjectedToDto();

	// 商品詳情：建構式投影一次撈齊，LEFT JOIN 帶分類名稱、不載入圖片 BLOB
	@Query("SELECT new com.farmily.product.dto.ProductDetailDTO("
			+ "p.productId, p.productName, p.retailPrice, p.groupPrice, "
			+ "p.unitPricingMeasure, p.description, p.isGroupBuy, "
			+ "s.subCatClassId, s.subCatClassName) "
			+ "FROM ProductVO p LEFT JOIN p.subCategoryVO s "
			+ "WHERE p.productId = :id")
	ProductDetailDTO findDetailById(@Param("id") Integer id);

	@Query("SELECT new com.farmily.product.dto.ProductGroupBuyDTO(p.productId, p.productName, p.groupPrice,p.unitPricingMeasure, p.description,s.subCatClassId, s.subCatClassName)"
		     + " from ProductVO p LEFT JOIN p.subCategoryVO s WHERE p.status = com.farmily.product.model.Status.ACTIVE "
		     + "AND p.isGroupBuy = true")
	List<ProductGroupBuyDTO> findGroupBuyProducts();
	
	
	// 只撈圖片這一個欄位（不載入整個 entity 的其他欄位）→ 讀圖時用
	@Query("SELECT p.productImage FROM ProductVO p WHERE p.productId = :id")
	byte[] findImageById(@Param("id") Integer id);

}
