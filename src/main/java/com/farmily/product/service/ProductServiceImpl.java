package com.farmily.product.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.farmily.product.dto.ProductDetailDTO;
import com.farmily.product.dto.ProductGroupBuyDTO;
import com.farmily.product.dto.ProductInsertDTO;
import com.farmily.product.dto.ProductManageDTO;
import com.farmily.product.dto.ProductSummaryDTO;
import com.farmily.product.dto.ProductUpdatedDTO;
import com.farmily.product.dto.SubCategoryOptionDTO;
import com.farmily.product.model.ProductRepository;
import com.farmily.product.model.ProductStatus;
import com.farmily.product.model.ProductVO;
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
	@Autowired
	private ProductImageService productImageService;
	@Autowired
	private ProductClickService productClickService;

	@Override
	@Transactional(readOnly = true)
	public Page<ProductSummaryDTO> getAllProducts(Pageable pageable) {
		return productRepository.findAllProjectedToDto(pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<ProductSummaryDTO> searchProducts(String keyword, Integer subCatClassId, Integer minPrice,
			Integer maxPrice, Integer farmerId, Pageable pageable) {
		if (keyword != null && keyword.isBlank()) {
			keyword = null;
		}
		return productRepository.searchProducts(keyword, subCatClassId, minPrice, maxPrice, farmerId, pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProductManageDTO> getMyProducts(Integer farmerId) {
		return productRepository.findMyProducts(farmerId);
	}

	@Transactional(readOnly = true)
	public ProductVO getProductReferenceById(Integer productId) {
		return productRepository.getReferenceById(productId);
	}

	// 存單筆產品
	@Override
	@CacheEvict(cacheNames = "hotProducts", allEntries = true) // 商品變動會影響首頁熱門清單的補滿內容，清快取
	public Integer addProduct(ProductInsertDTO dto, Integer farmerId) throws IOException {

		if (!subCategoryRepository.existsById(dto.getSubCatClassId())) {
			throw new IllegalArgumentException("查無此分類");
		}
		if (dto.getGroupPrice() != null && dto.getRetailPrice() != null && dto.getGroupPrice() > dto.getRetailPrice()) {
			throw new IllegalArgumentException("團購價不得高於原價");
		}

		if (Boolean.TRUE.equals(dto.getIsGroupBuy()) && dto.getGroupPrice() == null) {
			throw new IllegalArgumentException("開放團購時必須填寫團購價");
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

			String contentType = dto.getProductImage().getContentType();
			if (contentType == null || !contentType.startsWith("image/")) {
				throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "只能上傳圖片");
			}
			productVO.setProductImage(dto.getProductImage());
		}

		productVO.setFarmerId(farmerId);
		productVO.setStatus(ProductStatus.ACTIVE);

		productRepository.save(productVO);

		if (dto.getProductImages() != null && !dto.getProductImages().isEmpty())

		{

			productImageService.addProductImages(productVO.getProductId(), dto.getProductImages());
		}

		return productVO.getProductId();
	}

	// 更新價格+商品狀態(上,下架)
	@Override
	@CacheEvict(cacheNames = "hotProducts", allEntries = true) // 價格/上下架變動會影響首頁熱門清單，清快取
	public boolean updateProduct(Integer productId, ProductUpdatedDTO dto, Integer farmerId) {
		ProductVO product = productRepository.findById(productId).orElse(null);

		if (product == null) {
			return false;
		}
		if (!product.getFarmerId().equals(farmerId)) {
			throw new AccessDeniedException("無權限修改此商品");
		}

		// PATCH 可能只帶其中一個價格：沒帶的用商品現有的值，驗證的是「改完之後」的最終狀態
		Integer newRetailPrice = (dto.getRetailPrice() != null) ? dto.getRetailPrice() : product.getRetailPrice();
		Integer newGroupPrice = (dto.getGroupPrice() != null) ? dto.getGroupPrice() : product.getGroupPrice();
		if (newGroupPrice != null && newRetailPrice != null && newGroupPrice > newRetailPrice) {
			throw new IllegalArgumentException("團購價不得高於原價");
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

		if (!productRepository.existsById(productId)) {
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

	@Override
	@Transactional(readOnly = true)
	public List<ProductSummaryDTO> getHotProducts() {

		List<Integer> hotIds = productClickService.getHotProductIds();

		List<ProductSummaryDTO> result = new ArrayList<>();

		for (Integer id : hotIds) {
			ProductSummaryDTO dto = productRepository.findSummaryById(id);
			if (dto != null) {
				result.add(dto);
			}
		}
		return result;

	}

	// 首頁熱門清單：結算結果優先，不足 HOT_SIZE 筆就用上架中的商品「補滿」（含還沒結算過的冷啟動），
	// 首頁不會開天窗。注意管理員後台要的是原始結算結果，請走上面的 getHotProducts()，不要用這支。
	// 快取原因：這是首頁每次載入都打的熱路徑，而內容只在「結算 / 新增商品 / 改價改狀態」時才會變，
	// 這三處都掛了 @CacheEvict 清掉快取。
	@Override
	@Transactional(readOnly = true)
	@Cacheable("hotProducts")
	public List<ProductSummaryDTO> getHomeHotProducts() {

		List<ProductSummaryDTO> result = new ArrayList<>(getHotProducts());

		if (result.size() < ProductClickService.HOT_SIZE) {
			// 多抓一倍當備品，扣掉已上榜的再補，避免補到重複的商品
			List<ProductSummaryDTO> fillers = productRepository
					.findActiveSummaries(PageRequest.of(0, ProductClickService.HOT_SIZE * 2));
			for (ProductSummaryDTO filler : fillers) {
				if (result.size() >= ProductClickService.HOT_SIZE) {
					break;
				}
				boolean alreadyIn = false;
				for (ProductSummaryDTO r : result) {
					if (r.getProductId().equals(filler.getProductId())) {
						alreadyIn = true;
						break;
					}
				}
				if (!alreadyIn) {
					result.add(filler);
				}
			}
		}
		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public List<SubCategoryOptionDTO> getSubCategoryOptions() {

		return subCategoryRepository.findAllOptions();
	}
}
