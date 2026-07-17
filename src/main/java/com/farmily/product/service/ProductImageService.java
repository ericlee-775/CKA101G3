package com.farmily.product.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface ProductImageService {
	//單一查詢
	byte[] getProductImageById(Integer productImageId);
	
	List<Integer> getImageIdsByProductId(Integer productId);
	
	List<Integer> addProductImages(Integer productId, List<MultipartFile>files) throws IOException;
}
