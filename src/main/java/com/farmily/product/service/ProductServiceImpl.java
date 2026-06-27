package com.farmily.product.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.farmily.product.dto.ProductSummeryDTO;
import com.farmily.product.dto.ProductUpdatedDTO;
import com.farmily.product.model.ProductRepository;
import com.farmily.product.model.ProductVO;

@Service
@Transactional   // 類別層級：預設所有方法都包在讀寫交易裡（讀 → 改 → 存 同一個交易）
public class ProductServiceImpl implements ProductService{
	@Autowired
	private ProductRepository productRepository;

	@Override
	@Transactional(readOnly = true)   // 純查詢：唯讀交易，關掉 dirty checking、效能更好
	public List<ProductSummeryDTO> getAllProducts() {
	    return  productRepository.findAllProjectedToDto();
	}


	@Override
	public void addProduct(ProductVO productVO) {
		// 主鍵未設 → save() 執行 INSERT，並把 DB 自動產生的主鍵回填進 productVO
		productRepository.save(productVO);
	}

	@Override
	public boolean updateProductPrice(Integer productId, ProductUpdatedDTO dto) {
		// 負數檢查已移到 DTO 的 @Min(0) + controller 的 @Valid（宣告式驗證，進方法前就擋掉）
		// 先讀出舊的，查無就回 false（讓 controller 回 404）
		ProductVO product = productRepository.findById(productId).orElse(null);
		if (product == null) {
			return false;
		}

		// 局部更新（PATCH）：只覆蓋「有帶值」的價格欄位，其餘欄位一律原封不動
		if (dto.getRetailPrice() != null) {
			product.setRetailPrice(dto.getRetailPrice());
		}
		if (dto.getGroupPrice() != null) {
			product.setGroupPrice(dto.getGroupPrice());
		}

		// @Transactional 下，受管理 entity 會自動 dirty-check flush；save() 留著語意更明確
		productRepository.save(product);
		return true;
	}

	@Override
	@Transactional(readOnly = true)
	public byte[] getProductImageBytes(Integer productId) {
		return productRepository.findImageById(productId);
	}

}
