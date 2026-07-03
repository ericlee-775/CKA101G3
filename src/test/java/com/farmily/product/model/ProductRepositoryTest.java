package com.farmily.product.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.farmily.product.dto.ProductGroupBuyDTO;
import com.farmily.product.dto.ProductInsertDTO;
import com.farmily.product.service.ProductServiceImpl;

@SpringBootTest
@Transactional
class ProductRepositoryTest {

	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private ProductServiceImpl productServiceImpl;

	@Test
	public void getById() {
		ProductVO productVO = productRepository.findById(1).orElse(null);
		assertNotNull(productVO);
		assertEquals("屏東霸王農場香蕉", productVO.getProductName());

	}

	@Test
	public void getProductGroup() {
		List<ProductGroupBuyDTO> productGroupBuyDTO = productRepository.findGroupBuyProducts();
		assertNotNull(productGroupBuyDTO);

	}

	@Test
	public void testAddProduct() {
		ProductInsertDTO productDTO = new ProductInsertDTO();

		productDTO.setProductName("測試商品");
		productDTO.setRetailPrice(100);
		productDTO.setUnitPricingMeasure("顆");
		productDTO.setGroupPrice(90);
		
		

		ProductVO existingProduct = productRepository.findById(1).orElse(null);
		productDTO.setSubCatClassId(existingProduct.getSubCategoryVO().getSubCatClassId());

		productDTO.setIsGroupBuy(true);
		productDTO.setDescription("這是測試用的");
	
		
		Integer productId = productServiceImpl.addProduct(productDTO,1);
		assertNotNull(productId);
		
		ProductVO saveProduct = productRepository.findById(productId).orElse(null);
		assertNotNull(saveProduct);
		
		assertEquals("測試商品", saveProduct.getProductName());
		assertEquals(100, saveProduct.getRetailPrice());
		assertEquals("顆", saveProduct.getUnitPricingMeasure());
		assertEquals(90, saveProduct.getGroupPrice());
		assertEquals(true, saveProduct.getIsGroupBuy());
		assertEquals("這是測試用的", saveProduct.getDescription());
		assertEquals(existingProduct.getSubCategoryVO().getSubCatClassId(), saveProduct.getSubCategoryVO().getSubCatClassId());
		assertEquals(Status.INACTIVE, saveProduct.getStatus());
		assertNull(saveProduct.getProductImage());
	}
	@Test
	public void testAddProduct_WithMultipartImage(){
		
		ProductInsertDTO productDTO = new ProductInsertDTO();
		
		productDTO.setProductName("測試商品");
		productDTO.setRetailPrice(100);
		productDTO.setUnitPricingMeasure("顆");
		productDTO.setGroupPrice(90);


		ProductVO existingProduct = productRepository.findById(1).orElse(null);
		productDTO.setSubCatClassId(existingProduct.getSubCategoryVO().getSubCatClassId());

		productDTO.setIsGroupBuy(true);
		productDTO.setDescription("這是測試用的");
	
		
		MultipartFile mockMultiPartFile = new MockMultipartFile("productImage","test.jpg","image/jpeg","測試圖片內容".getBytes());
		productDTO.setProductImage(mockMultiPartFile);
		
		Integer productId = productServiceImpl.addProduct(productDTO,1);
		assertNotNull(productId);

		ProductVO saveProduct = productRepository.findById(productId).orElse(null);
		assertNotNull(saveProduct);
		
		assertEquals("測試商品", saveProduct.getProductName());
		assertEquals(100, saveProduct.getRetailPrice());
		assertEquals("顆", saveProduct.getUnitPricingMeasure());
		assertEquals(90, saveProduct.getGroupPrice());
		assertEquals(1, saveProduct.getFarmerId());
		assertEquals(true, saveProduct.getIsGroupBuy());
		assertEquals("這是測試用的", saveProduct.getDescription());
		assertEquals(Status.INACTIVE, saveProduct.getStatus());
		assertEquals(existingProduct.getSubCategoryVO().getSubCatClassId(), saveProduct.getSubCategoryVO().getSubCatClassId());
		assertArrayEquals("測試圖片內容".getBytes(), saveProduct.getProductImage());			
	}
	
	
	
}
