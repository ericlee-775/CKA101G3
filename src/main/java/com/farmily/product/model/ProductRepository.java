package com.farmily.product.model;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.farmily.product.dto.ProductDetailDTO;
import com.farmily.product.dto.ProductGroupBuyDTO;
import com.farmily.product.dto.ProductManageDTO;
import com.farmily.product.dto.ProductSummaryDTO;

import groovyjarjarpicocli.CommandLine.Parameters;

public interface ProductRepository extends JpaRepository<ProductVO, Integer> {

	// 全部商品
	@Query(value = "SELECT new com.farmily.product.dto.ProductSummaryDTO("
			+ "p.productId, p.retailPrice, p.unitPricingMeasure, p.productName,f.farmName) "
			+ "FROM ProductVO p JOIN p.farmer f "
			+ "WHERE p.status = com.farmily.product.model.ProductStatus.ACTIVE "
			+ "AND f.farmerStatus = ACTIVE",
		countQuery = "SELECT count(p) FROM ProductVO p JOIN p.farmer f "
			+ "WHERE p.status = com.farmily.product.model.ProductStatus.ACTIVE "
			+ "AND f.farmerStatus = ACTIVE")
	Page<ProductSummaryDTO> findAllProjectedToDto(Pageable pageable);

	// 複合查詢
	@Query(value = "SELECT new com.farmily.product.dto.ProductSummaryDTO("
			+ "p.productId, p.retailPrice, p.unitPricingMeasure, p.productName,f.farmName,f.farmerId) "
			+ "FROM ProductVO p JOIN p.subCategoryVO s JOIN p.farmer f "
			+ "WHERE p.status = com.farmily.product.model.ProductStatus.ACTIVE "
			+ "AND (:keyword IS NULL OR p.productName LIKE CONCAT('%', :keyword, '%')) "
			+ "AND (:subCatClassId IS NULL OR s.subCatClassId = :subCatClassId) "
			+ "AND (:minPrice IS NULL OR p.retailPrice >= :minPrice) "
			+ "AND (:maxPrice IS NULL OR p.retailPrice <= :maxPrice) "
			+ "AND (:farmerId IS NULL OR f.farmerId = :farmerId) "
			+ "AND f.farmerStatus = ACTIVE",
		countQuery = "SELECT count(p) FROM ProductVO p JOIN p.subCategoryVO s JOIN p.farmer f "
			+ "WHERE p.status = com.farmily.product.model.ProductStatus.ACTIVE "
			+ "AND (:keyword IS NULL OR p.productName LIKE CONCAT('%', :keyword, '%')) "
			+ "AND (:subCatClassId IS NULL OR s.subCatClassId = :subCatClassId) "
			+ "AND (:minPrice IS NULL OR p.retailPrice >= :minPrice) "
			+ "AND (:maxPrice IS NULL OR p.retailPrice <= :maxPrice) "
			+ "AND (:farmerId IS NULL OR f.farmerId = :farmerId) "
			+ "AND f.farmerStatus = ACTIVE")
	Page<ProductSummaryDTO> searchProducts(@Param("keyword") String keyword,
			@Param("subCatClassId") Integer subCatClassId,
			@Param("minPrice") Integer minPrice,
			@Param("maxPrice") Integer maxPrice,
			@Param("farmerId") Integer farmerId,
			Pageable pageable);

	// 農夫管理自己商品（小農端）
	// 這裡不過濾 farmerStatus
	@Query("SELECT new com.farmily.product.dto.ProductManageDTO(p.productId, p.productName, p.retailPrice, "
			+ "p.groupPrice, p.unitPricingMeasure, p.status) "
			+ "FROM ProductVO p WHERE p.farmerId = :farmerId")
	List<ProductManageDTO> findMyProducts(@Param("farmerId") Integer farmerId);

	// 單一查詢不載入圖片 BLOB
	
	@Query("SELECT new com.farmily.product.dto.ProductDetailDTO("
			+ "p.productId, p.productName, p.retailPrice, p.groupPrice, "
			+ "p.unitPricingMeasure, p.description, p.isGroupBuy, s.subCatClassId, s.subCatClassName,f.farmName) "
			+ "FROM ProductVO p LEFT JOIN p.subCategoryVO s JOIN p.farmer f "
			+ "WHERE p.productId = :id AND p.status = com.farmily.product.model.ProductStatus.ACTIVE "
			+ "AND f.farmerStatus = ACTIVE")
	ProductDetailDTO findDetailById(@Param("id") Integer id);

	// 團購清單（買家端）：商品上架 + 小農未停權 + 有開團購
	@Query("SELECT new com.farmily.product.dto.ProductGroupBuyDTO(p.productId, p.productName, p.groupPrice, "
			+ "p.unitPricingMeasure, p.description, s.subCatClassId, s.subCatClassName) "
			+ "FROM ProductVO p LEFT JOIN p.subCategoryVO s JOIN p.farmer f "
			+ "WHERE p.status = com.farmily.product.model.ProductStatus.ACTIVE "
			+ "AND f.farmerStatus = ACTIVE "
			+ "AND p.isGroupBuy = true")
	List<ProductGroupBuyDTO> findGroupBuyProducts();

	// 只撈圖片這一個欄位（不載入整個 entity 的其他欄位）→ 讀圖時用
	@Query("SELECT p.productImage FROM ProductVO p WHERE p.productId = :id")
	byte[] findImageById(@Param("id") Integer id);

}
