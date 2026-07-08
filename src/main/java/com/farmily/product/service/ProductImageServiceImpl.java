package com.farmily.product.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.farmily.product.model.ProductImageRepository;
import com.farmily.product.model.ProductImageVO;

@Service
public class ProductImageServiceImpl implements ProductImageService{

	@Autowired
	private ProductImageRepository productImageRepository;
	//單一查詢圖片
	@Override
	public byte[] getProductImageById(Integer productImageId) {
		return productImageRepository.findImageById(productImageId);
	}
	@Override
	public List<Integer> getImageIdsByProductId(Integer productId) {
		return productImageRepository.findImageIdsByProductId(productId);
	}
	
	
	
}
