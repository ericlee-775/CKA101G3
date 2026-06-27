package com.farmily.product.service;
import java.util.List;

import com.farmily.product.dto.ProductSummeryDTO;
import com.farmily.product.model.ProductVO;

public interface ProductService {

	void addProduct(ProductVO productVO);
	
	void updateProduct(Integer productId,ProductVO productVO);
	
	List<ProductSummeryDTO> getAllProducts(); 

	ProductVO getProductById(Integer productId);


}
