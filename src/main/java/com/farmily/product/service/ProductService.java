package com.farmily.product.service;
import java.util.List;

import com.farmily.product.dto.ProductDetailDTO;
import com.farmily.product.dto.ProductGroupBuyDTO;
import com.farmily.product.dto.ProductSummeryDTO;
import com.farmily.product.dto.ProductUpdatedDTO;
import com.farmily.product.model.ProductVO;

public interface ProductService {

	void addProduct(ProductVO productVO);
	
	boolean updateProductPrice(Integer productId, ProductUpdatedDTO dto);

	List<ProductSummeryDTO> getAllProducts();
	
	ProductDetailDTO getProductDetail(Integer productId);

	byte[] getProductImageBytes(Integer productId);
	
	List<ProductGroupBuyDTO> getAllGroupProducts();
}
