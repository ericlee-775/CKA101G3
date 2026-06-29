package com.farmily.product.service;

import java.util.List;

import com.farmily.product.dto.ProductDetailDTO;
import com.farmily.product.dto.ProductSummeryDTO;
import com.farmily.product.dto.ProductUpdatedDTO;
import com.farmily.product.model.ProductVO;


public interface ProductService {

	void addProduct(ProductVO productVO);

	// 只更新價格(零售價/團購價)；有找到並更新回 true，查無商品回 false
	boolean updateProductPrice(Integer productId, ProductUpdatedDTO dto);

	List<ProductSummeryDTO> getAllProducts();

	// 商品詳情；查無回 null（讓 controller 回 404）
	ProductDetailDTO getProductDetail(Integer productId);

	// 只取圖片 bytes（讀圖用，不載入整個 entity）
	byte[] getProductImageBytes(Integer productId);
	
	

	//收藏是「某個用戶」收藏「某個商品」,回傳 boolean 也合理（成功回 true，已經收藏過回 false）
	boolean addWishList(Integer productId, Integer userId);
	
	boolean deleteWishList(Integer productId, Integer userId);
	
	List<ProductSummeryDTO> getAllWishLists(Integer userId);

}
