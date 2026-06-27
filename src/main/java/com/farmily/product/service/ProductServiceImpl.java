package com.farmily.product.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.farmily.product.dto.ProductSummeryDTO;
import com.farmily.product.dto.ProductUpdatedDTO;
import com.farmily.product.model.ProductRepository;
import com.farmily.product.model.ProductVO;

@Service
public class ProductServiceImpl implements ProductService{
	@Autowired
	private ProductRepository productRepository;

	@Override
	public List<ProductSummeryDTO> getAllProducts() {
	    return  productRepository.findAllProjectedToDto();
	}


	@Override
	public void addProduct(ProductVO productVO) {
		// save() 新增後會回傳含自動產生主鍵的物件
		productRepository.save(productVO);
	}

	@Override
	public boolean updateProductPrice(Integer productId, ProductUpdatedDTO dto) {
		// 先讀出舊的，查無就回 false（讓 controller 回 404）
		ProductVO product = productRepository.findById(productId).orElse(null);
		if (product == null) {
			return false;
		}

		// 局部更新（PATCH）：只覆蓋「有帶值」的價格欄位，其餘欄位一律原封不動
		if (dto.getRetailPrice() != null) {
			product.setRetailPrice(dto.getRetailPrice());
		}
		if (dto.getGroupPrice() != null) {
			product.setGroupPrice(dto.getGroupPrice());
		}

		productRepository.save(product);
		return true;
	}

	@Override
	public ProductVO getProductById(Integer productId) {
		return productRepository.findById(productId).orElse(null);
	}

	@Override
	public byte[] getProductImageBytes(Integer productId) {
		return productRepository.findImageById(productId);
	}
	

	
}
