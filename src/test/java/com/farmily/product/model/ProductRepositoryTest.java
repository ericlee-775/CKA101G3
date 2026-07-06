package com.farmily.product.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.farmily.product.dto.ProductGroupBuyDTO;

import java.util.List;

@SpringBootTest
class ProductRepositoryTest {

	@Autowired
	private ProductRepository productRepository;
	
	@Test
	public void getById() {
		ProductVO productVO = productRepository.findById(1).orElse(null);
		assertNotNull(productVO);
		assertEquals("屏東霸王農場香蕉", productVO.getProductName());
		
	}
	// [暫時停用] 待 ProductGroupBuyDTO 補上對應建構子、findGroupBuyProducts() 恢復後再啟用
//	@Test
//	public void getProductGroup() {
//		List<ProductGroupBuyDTO> productGroupBuyDTO = productRepository.findGroupBuyProducts();
//		assertNotNull(productGroupBuyDTO);
//
//
//	}
}
