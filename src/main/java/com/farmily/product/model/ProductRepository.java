package com.farmily.product.model;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductVO, Integer>{

	void updateProduct(Integer productId, ProductVO productVO);

	Integer addProduct(ProductVO productVO);

	ProductVO getProductById(Integer productId);


}