package com.farmily.product.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import com.farmily.product.dto.ProductDTO;
import com.farmily.product.model.ProductRepository;
import com.farmily.product.model.ProductVO;

@Service
public class ProductServiceImpl implements ProductService{
	@Autowired
	private ProductRepository productRepository;

	@Override
	public List<ProductDTO> getAllProducts() {
	    List<ProductVO> voList = productRepository.findAll();
	    
	    // 將 VO 清單轉換為 DTO 清單
	    return voList.stream().map(vo -> {
	        ProductDTO dto = new ProductDTO();
	        dto.setProductId(vo.getProductId());
	        dto.setProductName(vo.getProductName());
	        dto.setRetailPrice(vo.getRetailPrice());
	        dto.setUnitPricingMeasure(vo.getUnitPricingMeasure());
	        return dto;
	    }).toList();
	}

	

	
}
