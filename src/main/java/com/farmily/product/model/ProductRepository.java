package com.farmily.product.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.farmily.product.dto.ProductSummeryDTO;

public interface ProductRepository extends JpaRepository<ProductVO, Integer>{
	@Query("SELECT new com.farmily.product.dto.ProductDTO(p.productId, p.retailPrice, p.unitPricingMeasure, p.productName) FROM ProductVO p")
	List<ProductSummeryDTO> findAllProjectedToDto();

	// 只撈圖片這一個欄位（不載入整個 entity 的其他欄位）→ 讀圖時用
	@Query("SELECT p.productImage FROM ProductVO p WHERE p.productId = :id")
	byte[] findImageById(@Param("id") Integer id);

}
