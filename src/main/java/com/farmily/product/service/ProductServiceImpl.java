package com.farmily.product.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.farmily.product.dto.ProductDetailDTO;
import com.farmily.product.dto.ProductGroupBuyDTO;
import com.farmily.product.dto.ProductSummeryDTO;
import com.farmily.product.dto.ProductUpdatedDTO;
import com.farmily.product.model.ProductRepository;
import com.farmily.product.model.ProductVO;
import com.farmily.product.model.WishListId;
import com.farmily.product.model.WishListRepository;
import com.farmily.product.model.WishListVO;

@Service
@Transactional   
public class ProductServiceImpl implements ProductService{

	@Autowired
	private ProductRepository productRepository;
	//取所有產品
	@Autowired
	private WishListRepository wishListRepository;

	@Override
	@Transactional(readOnly = true) 
	public List<ProductSummeryDTO> getAllProducts() {
		return productRepository.findAllProjectedToDto();
	}
	//存單筆產品
	@Override
	public void addProduct(ProductVO productVO) {
		productRepository.save(productVO);
	}
	//更新價格
	@Override
	public boolean updateProductPrice(Integer productId, ProductUpdatedDTO dto) {
		ProductVO product = productRepository.findById(productId).orElse(null);
		if (product == null) {
			return false;
		}
		if (dto.getRetailPrice() != null) {
			product.setRetailPrice(dto.getRetailPrice());
		}
		if (dto.getGroupPrice() != null) {
			product.setGroupPrice(dto.getGroupPrice());
		}
		productRepository.save(product);
		return true;
	}
	//取封面圖片
	@Override
	@Transactional(readOnly = true)
	public byte[] getProductImageBytes(Integer productId) {
		return productRepository.findImageById(productId);
	}
	//取單筆產品
	@Override
	@Transactional(readOnly = true)
	public ProductDetailDTO getProductDetail(Integer productId) {
		return productRepository.findDetailById(productId);
	}
	//給團購用的全部查詢
	@Override
	@Transactional(readOnly = true)
	public List<ProductGroupBuyDTO> getAllGroupBuyProducts() {
		return productRepository.findGroupBuyProducts();
	}
	//給團購用的單筆查詢
	@Override
	@Transactional(readOnly = true)
	public ProductVO getGroupBuyProductById(Integer productId) {
	    return productRepository.findById(productId)
	                           .orElse(null);   // 找不到就回 null
	}
	@Override
	public boolean addWishList(Integer productId, Integer userId) {

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
	public List<ProductSummeryDTO> getAllWishLists(Integer userId) {

		return wishListRepository.findWishListByUserId(userId);
	}
}
