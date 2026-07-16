package com.farmily.product.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.farmily.product.model.ProductImageRepository;
import com.farmily.product.model.ProductImageVO;
import com.farmily.product.model.ProductRepository;




@Service
public class ProductImageServiceImpl implements ProductImageService {

	@Autowired
	private ProductImageRepository productImageRepository;
	@Autowired
	private ProductRepository productRepository;

	// 單一查詢圖片
	@Override
	public byte[] getProductImageById(Integer productImageId) {
		return productImageRepository.findImageById(productImageId);
	}

	@Override
	public List<Integer> getImageIdsByProductId(Integer productId) {
		return productImageRepository.findImageIdsByProductId(productId);
	}

	@Override
	@Transactional
	public List<Integer> addProductImages(Integer productId, List<MultipartFile> files) throws IOException {

		
		if (!productRepository.existsById(productId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商品不存在: " + productId);
		}
		// 2. 逐檔驗證，並轉成 byte[]
		List<byte[]>imageList = new ArrayList<>();
		for (MultipartFile file : files) {
			if (file.isEmpty()) {
				continue; // 空檔跳過
			}
			String contentType = file.getContentType();
			if (contentType == null || !contentType.startsWith("image/")) {
				throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "只能上傳圖片");
			}
			imageList.add(file.getBytes());
		}
		if (imageList.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "沒有有效的圖片檔");
		}
		for(byte[]image:imageList) {
			
			ProductImageVO productImage = new ProductImageVO() ;
			productImage.setProductId(productId);
			productImage.setProductImage(image);
			productImageRepository.save(productImage);
		}
		
		return getImageIdsByProductId (productId);
	}

}
