package com.farmily.product.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.farmily.product.dto.ProductGroupBuyDTO;
import com.farmily.product.dto.ProductInsertDTO;
import com.farmily.product.dto.ProductSummaryDTO;
import com.farmily.product.dto.ProductUpdatedDTO;
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
	public void testAddProduct() throws IOException {
		ProductInsertDTO productDTO = new ProductInsertDTO();

		productDTO.setProductName("測試商品");
		productDTO.setRetailPrice(100);
		productDTO.setUnitPricingMeasure("顆");
		productDTO.setGroupPrice(90);

		ProductVO existingProduct = productRepository.findById(1).orElse(null);
		productDTO.setSubCatClassId(existingProduct.getSubCategoryVO().getSubCatClassId());

		productDTO.setIsGroupBuy(true);
		productDTO.setDescription("這是測試用的");

		Integer productId = productServiceImpl.addProduct(productDTO, 1);
		assertNotNull(productId);

		ProductVO saveProduct = productRepository.findById(productId).orElse(null);
		assertNotNull(saveProduct);

		assertEquals("測試商品", saveProduct.getProductName());
		assertEquals(100, saveProduct.getRetailPrice());
		assertEquals("顆", saveProduct.getUnitPricingMeasure());
		assertEquals(90, saveProduct.getGroupPrice());
		assertEquals(true, saveProduct.getIsGroupBuy());
		assertEquals("這是測試用的", saveProduct.getDescription());
		assertEquals(existingProduct.getSubCategoryVO().getSubCatClassId(),
				saveProduct.getSubCategoryVO().getSubCatClassId());
		assertEquals(ProductStatus.ACTIVE, saveProduct.getStatus());
		assertNull(saveProduct.getProductImage());
	}

	@Test
	public void testAddProduct_WithMultipartImage() throws IOException {

		ProductInsertDTO productDTO = new ProductInsertDTO();

		productDTO.setProductName("測試商品");
		productDTO.setRetailPrice(100);
		productDTO.setUnitPricingMeasure("顆");
		productDTO.setGroupPrice(90);

		ProductVO existingProduct = productRepository.findById(1).orElse(null);
		productDTO.setSubCatClassId(existingProduct.getSubCategoryVO().getSubCatClassId());

		productDTO.setIsGroupBuy(true);
		productDTO.setDescription("這是測試用的");

		MultipartFile mockMultiPartFile = new MockMultipartFile("productImage", "test.jpg", "image/jpeg",
				"測試圖片內容".getBytes());
		productDTO.setProductImage(mockMultiPartFile);

		Integer productId = productServiceImpl.addProduct(productDTO, 1);
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
		assertEquals(ProductStatus.ACTIVE, saveProduct.getStatus());
		assertEquals(existingProduct.getSubCategoryVO().getSubCatClassId(),
				saveProduct.getSubCategoryVO().getSubCatClassId());
		assertArrayEquals("測試圖片內容".getBytes(), saveProduct.getProductImage());
	}


	@Test
	public void testUpdateProduct_ProductNotFound() {

		ProductUpdatedDTO productDTO = new ProductUpdatedDTO();
		productDTO.setRetailPrice(100);
		productDTO.setGroupPrice(200);
		assertFalse(productServiceImpl.updateProduct(-1, productDTO, 100));

	}

	@Test
	public void testUpdateProduct_NotOwner() {
		ProductUpdatedDTO productDTO = new ProductUpdatedDTO();
		productDTO.setRetailPrice(100);
		productDTO.setGroupPrice(200);
		assertThrows(AccessDeniedException.class, () -> {
			productServiceImpl.updateProduct(1, productDTO, -1);
		});
	}

	@Test
	public void testUpdateProductPrice_Owner() {
		ProductUpdatedDTO productDTO = new ProductUpdatedDTO();
		productDTO.setRetailPrice(100);
		productDTO.setGroupPrice(200);
		ProductVO existingProduct = productRepository.findById(1).orElse(null);
		Integer ownerId = existingProduct.getFarmerId();
		assertTrue(productServiceImpl.updateProduct(1, productDTO, ownerId));
		ProductVO updateProduct = productRepository.findById(1).orElse(null);
		assertEquals(100, updateProduct.getRetailPrice());
		assertEquals(200, updateProduct.getGroupPrice());

	}

	@Test
	public void testAddWishList() {
		productServiceImpl.deleteWishList(1, 1);
		assertTrue(productServiceImpl.addWishList(1, 1));
		assertFalse(productServiceImpl.addWishList(1, 1));
	}

	@Test
	public void testDeleteWishList() {

		productServiceImpl.addWishList(1, 1);
		assertTrue(productServiceImpl.deleteWishList(1, 1));
		assertFalse(productServiceImpl.deleteWishList(1, 1));

	}

	@Test
	public void testGetAllWishLists() {

		productServiceImpl.addWishList(1, 1);
		List<ProductSummaryDTO> existingWishList = productServiceImpl.getAllWishLists(1);
		assertNotNull(existingWishList);
		boolean wishList = existingWishList.stream().anyMatch(item -> item.getProductId().equals(1));
		assertTrue(wishList);

	}

	@Test
	public void testUpdateProduct_StatusOnly() {

		ProductVO existingProduct = productRepository.findById(1).orElse(null);
		Integer ownerId = existingProduct.getFarmerId();
		Integer originalRetailPrice = existingProduct.getRetailPrice();
		Integer originalGroupPrice = existingProduct.getGroupPrice();

		ProductUpdatedDTO productDTO = new ProductUpdatedDTO();
		productDTO.setStatus(ProductStatus.INACTIVE);

		assertTrue(productServiceImpl.updateProduct(1, productDTO, ownerId));

		ProductVO updateProduct = productRepository.findById(1).orElse(null);
		assertEquals(ProductStatus.INACTIVE, updateProduct.getStatus());
		assertEquals(originalRetailPrice, updateProduct.getRetailPrice());
		assertEquals(originalGroupPrice, updateProduct.getGroupPrice());

	}

}