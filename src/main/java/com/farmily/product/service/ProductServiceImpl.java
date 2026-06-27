package com.farmily.product.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.farmily.product.dto.ProductSummeryDTO;
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
	public void updateProduct(Integer productId, ProductVO productVO) {
		byte[] img = productVO.getProductImage();

		// null 或空陣列 → 代表前端沒選新圖 → 沿用 DB 裡的舊圖
		if (img == null || img.length == 0) {
			ProductVO old = productRepository.findById(productId).orElse(null);
			if (old != null) {
				productVO.setProductImage(old.getProductImage());
			}
		}

		// 指定主鍵後 save() 會執行更新（而非新增）
		productVO.setProductId(productId);
		productRepository.save(productVO);
	}

	@Override
	public ProductVO getProductById(Integer productId) {
		return productRepository.findById(productId).orElse(null);
	}
	

	
}
