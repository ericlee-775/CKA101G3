package com.farmily.product.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.farmily.product.dto.ProductDetailDTO;
import com.farmily.product.dto.ProductGroupBuyDTO;
import com.farmily.product.dto.ProductInsertDTO;
import com.farmily.product.dto.ProductManageDTO;
import com.farmily.product.dto.ProductSummaryDTO;
import com.farmily.product.dto.ProductUpdatedDTO;
import com.farmily.product.model.ProductRepository;
import com.farmily.product.model.ProductVO;
import com.farmily.product.model.ProductStatus;
import com.farmily.product.model.SubCategoryRepository;
import com.farmily.product.model.SubCategoryVO;
import com.farmily.product.model.WishListId;
import com.farmily.product.model.WishListRepository;
import com.farmily.product.model.WishListVO;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private WishListRepository wishListRepository;
	@Autowired
	private SubCategoryRepository subCategoryRepository;

	@Override
	@Transactional(readOnly = true)
	public Page<ProductSummaryDTO> getAllProducts(Pageable pageable) {
		return productRepository.findAllProjectedToDto(pageable);
	}
	@Override
	@Transactional(readOnly = true)
	public Page<ProductSummaryDTO> searchProducts(String keyword, Integer subCatClassId,
			Integer minPrice, Integer maxPrice,Integer farmerId,Pageable pageable) {
		if (keyword != null && keyword.isBlank()) {
			keyword = null;
		}
		return productRepository.searchProducts(keyword, subCatClassId, minPrice, maxPrice,farmerId, pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProductManageDTO> getMyProducts(Integer farmerId){
		return productRepository.findMyProducts(farmerId);
	}
	

	@Transactional(readOnly = true)
	public ProductVO getProductReferenceById(Integer productId) {
		return productRepository.getReferenceById(productId);
	}

	// 存單筆產品
	@Override
	public Integer addProduct(ProductInsertDTO dto, Integer farmerId) {

		if(!subCategoryRepository.existsById(dto.getSubCatClassId())) {
			throw new IllegalArgumentException("查無此分類");
		}
		
		ProductVO productVO = new ProductVO();
		productVO.setProductName(dto.getProductName());
		productVO.setRetailPrice(dto.getRetailPrice());
		productVO.setGroupPrice(dto.getGroupPrice());
		productVO.setUnitPricingMeasure(dto.getUnitPricingMeasure());
		productVO.setIsGroupBuy(dto.getIsGroupBuy());
		productVO.setDescription(dto.getDescription());

		SubCategoryVO subCategoryVO = new SubCategoryVO();
		subCategoryVO.setSubCatClassId(dto.getSubCatClassId());
		productVO.setSubCategoryVO(subCategoryVO);

		if (dto.getProductImage() != null && !dto.getProductImage().isEmpty()) {

			productVO.setProductImage(dto.getProductImage());
		}

		productVO.setFarmerId(farmerId);
		productVO.setStatus(ProductStatus.ACTIVE);

		productRepository.save(productVO);

		return productVO.getProductId();
	}

	// 更新價格+商品狀態(上,下架)
	@Override
	public boolean updateProduct(Integer productId, ProductUpdatedDTO dto, Integer farmerId) {
		ProductVO product = productRepository.findById(productId).orElse(null);

		if (product == null) {
			return false;
		}
		if (!product.getFarmerId().equals(farmerId)) {
			throw new AccessDeniedException("無權限修改此商品");
		}
		if (dto.getRetailPrice() != null) {
			product.setRetailPrice(dto.getRetailPrice());
		}
		if (dto.getGroupPrice() != null) {
			product.setGroupPrice(dto.getGroupPrice());
		}
		if (dto.getStatus() != null) {
			product.setStatus(dto.getStatus());
		}
		productRepository.save(product);
		return true;
	}

	// 取封面圖片
	@Override
	@Transactional(readOnly = true)
	public byte[] getProductImageBytes(Integer productId) {
		return productRepository.findImageById(productId);
	}

	// 取單筆產品
	@Override
	@Transactional(readOnly = true)
	public ProductDetailDTO getProductDetail(Integer productId) {
		return productRepository.findDetailById(productId);
	}


	// 給團購用的全部查詢
	@Override
	@Transactional(readOnly = true)
	public List<ProductGroupBuyDTO> getAllGroupBuyProducts() {
		return productRepository.findGroupBuyProducts();
	}

	// 給團購用的單筆查詢

	@Override
	@Transactional(readOnly = true)
	public ProductVO getGroupBuyProductById(Integer productId) {
		return productRepository.findById(productId).orElse(null); // 找不到就回 null
	}

	@Override
	public boolean addWishList(Integer productId, Integer userId) {

		if (!productRepository.existsById(productId)) {
			throw new IllegalArgumentException("查無此商品");
		}

		if (wishListRepository.existsByProductIdAndUserId(productId, userId)) {

			return false;
		} else {

			WishListVO wishListVO = new WishListVO();
			wishListVO.setProductId(productId);
			wishListVO.setUserId(userId);
			wishListRepository.save(wishListVO);

		}
		return true;
	}

	@Override
	public boolean deleteWishList(Integer productId, Integer userId) {

		if(!productRepository.existsById(productId)) {
			throw new IllegalArgumentException("查無此商品");
		}
		
		
		if (wishListRepository.existsByProductIdAndUserId(productId, userId)) {

			WishListId id = new WishListId();
			id.setProductId(productId);
			id.setUserId(userId);
			wishListRepository.deleteById(id);
			return true;
		} else {
			return false;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProductSummaryDTO> getAllWishLists(Integer userId) {

		return wishListRepository.findWishListByUserId(userId);
	}
}
