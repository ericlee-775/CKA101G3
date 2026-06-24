package com.farmily.product.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.farmily.product.model.ProductRepository;
import com.farmily.product.model.ProductVO;

@Service
public class ProductServiceimpl implements ProductService{
	@Autowired
	private ProductRepository productRepository;

	@Override
	public List<ProductVO> getAllProducts() {
		 return productRepository.findAll();
	}

	@Override
	public Integer addProduct(ProductVO productVO) {
		
		return productRepository.addProduct(productVO);
	}

	@Override
	public void updateProduct(Integer productId, ProductVO productVO) {
		
		productRepository.updateProduct(productId, productVO);
	}

	@Override
	public ProductVO getProductById(Integer productId) {
		
		return productRepository.getProductById(productId);
	}
	

	
}
