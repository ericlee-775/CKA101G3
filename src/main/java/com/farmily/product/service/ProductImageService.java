package com.farmily.product.service;

import java.util.List;

public interface ProductImageService {
	//單一查詢
	byte[] getProductImageById(Integer productImageId);
	
	List<Integer> getImageIdsByProductId(Integer productId);
}
