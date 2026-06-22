package com.farmily.product.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.farmily.product.model.ProductRepository;
import com.farmily.product.model.ProductVO;

@Controller
public class ProductServiceimpl implements ProductService{
	@Autowired
	private ProductRepository productRepository;

	@Override
	public List<ProductVO> getAllProducts() {
		 return productRepository.findAll();
	}
	

	
}
