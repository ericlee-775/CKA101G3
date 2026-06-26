package com.farmily.product.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.farmily.product.dto.ProductDTO;

public interface ProductRepository extends JpaRepository<ProductVO, Integer>{
	@Query("SELECT new com.farmily.product.dto.ProductDTO(p.productId, p.retailPrice, p.unitPricingMeasure, p.productName) FROM ProductVO p")
	List<ProductDTO> findAllProjectedToDto();
	

}
