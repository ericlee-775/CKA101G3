package com.farmily.product.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
}
