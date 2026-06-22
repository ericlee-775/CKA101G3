package com.farmily.product.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.farmily.product.model.ProductVO;
import com.farmily.product.service.ProductService;

@RestController
public class ProductController {
	@Autowired
	private ProductService productService;
	
	@GetMapping("/product")
	public List<ProductVO> getAllProduct(){
		return productService.getAllProducts();
	}
}
