package com.farmily.product.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductImageRepository extends JpaRepository<ProductImageVO,Integer>{
	//用id查詢單一圖片
	@Query("SELECT p.productImage FROM ProductImageVO p WHERE p.productImageId = :id")
	byte[] findImageById(@Param("id") Integer id);
	
	@Query("SELECT p.productImageId FROM ProductImageVO p WHERE p.productId = :productId ORDER BY p.productImageId")
	List<Integer> findImageIdsByProductId(@Param("productId") Integer productId);
}
