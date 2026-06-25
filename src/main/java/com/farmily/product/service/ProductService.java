package com.farmily.product.service;

import java.util.List;

import com.farmily.product.dto.ProductDTO;
import com.farmily.product.model.ProductVO;

public interface ProductService {

	Integer addProduct(ProductVO productVO);
	
	void updateProduct(Integer productId,ProductVO productVO);
	
	List<ProductDTO> getAllProducts(); 

	ProductVO getProductById(Integer productId);

}
