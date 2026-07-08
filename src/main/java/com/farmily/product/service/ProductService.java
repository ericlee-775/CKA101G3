package com.farmily.product.service;

import java.util.List;

import com.farmily.product.dto.ProductDetailDTO;
import com.farmily.product.dto.ProductGroupBuyDTO;
import com.farmily.product.dto.ProductInsertDTO;
import com.farmily.product.dto.ProductManageDTO;
import com.farmily.product.dto.ProductSummeryDTO;
import com.farmily.product.dto.ProductUpdatedDTO;
import com.farmily.product.model.ProductVO;


public interface ProductService {

	Integer addProduct(ProductInsertDTO dto, Integer farmerId);
	
	boolean updateProduct(Integer productId, ProductUpdatedDTO dto, Integer farmerId);

	List<ProductSummeryDTO> getAllProducts();
	
	List<ProductManageDTO> getMyProducts(Integer farmerId);
	
	ProductDetailDTO getProductDetail(Integer productId);

	byte[] getProductImageBytes(Integer productId);

	// [暫時停用] 待 ProductGroupBuyDTO 補上對應建構子後再啟用
	List<ProductGroupBuyDTO> getAllGroupBuyProducts();
	
	ProductVO getGroupBuyProductById(Integer ProductId);

	boolean addWishList(Integer productId, Integer userId);
	
	boolean deleteWishList(Integer productId, Integer userId);
	
	List<ProductSummeryDTO> getAllWishLists(Integer userId);

}
