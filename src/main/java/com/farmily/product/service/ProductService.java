package com.farmily.product.service;
import java.util.List;

import com.farmily.product.dto.ProductSummeryDTO;
import com.farmily.product.dto.ProductUpdatedDTO;
import com.farmily.product.model.ProductVO;

public interface ProductService {

	void addProduct(ProductVO productVO);

	// 只更新價格(零售價/團購價)；有找到並更新回 true，查無商品回 false
	boolean updateProductPrice(Integer productId, ProductUpdatedDTO dto);

	List<ProductSummeryDTO> getAllProducts();

	ProductVO getProductById(Integer productId);

	// 只取圖片 bytes（讀圖用，不載入整個 entity）
	byte[] getProductImageBytes(Integer productId);

}
