package com.farmily.product.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
	    // 1. 從資料庫查出真實的 Entity (VO) 列表
	    List<ProductVO> voList = productRepository.findAll();
	    
	    // 2. 建立一個空的 DTO 列表準備儲存轉換後的結果
	    List<ProductDTO> dtoList = new java.util.ArrayList<>();
	    
	    // 3. 透過迴圈，把每一個 VO 的資料複製到 DTO 中
	    for (ProductVO vo : voList) {
	        ProductDTO dto = new ProductDTO();
	        
	        // 使用 Spring 內建的 BeanUtils 自動複製相同名稱的欄位 (例如 id, name, price 等)
	        org.springframework.beans.BeanUtils.copyProperties(vo, dto);
	        
	        dtoList.add(dto);
	    }
	    
	    // 4. 回傳轉換完成的 DTO 列表
	    return dtoList;
	}


	@Override
	public Integer addProduct(ProductVO productVO) {
		// save() 新增後會回傳含自動產生主鍵的物件
		ProductVO saved = productRepository.save(productVO);
		return saved.getProductId();
	}

	@Override
	public void updateProduct(Integer productId, ProductVO productVO) {
		byte[] img = productVO.getProductImage();

		// null 或空陣列 → 代表前端沒選新圖 → 沿用 DB 裡的舊圖
		if (img == null || img.length == 0) {
			ProductVO old = productRepository.findById(productId).orElse(null);
			if (old != null) {
				productVO.setProductImage(old.getProductImage());
			}
		}

		// 指定主鍵後 save() 會執行更新（而非新增）
		productVO.setProductId(productId);
		productRepository.save(productVO);
	}

	@Override
	public ProductVO getProductById(Integer productId) {
		return productRepository.findById(productId).orElse(null);
	}
	

	
}
