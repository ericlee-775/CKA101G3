package com.farmily.product.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductOrderRepository extends JpaRepository<ProductOrderVO, Integer> {
	
	// 取得該會員所有訂單
	public Page<ProductOrderVO> findByUserId(Integer userId, Pageable pageable);

	// 取得屬於該小農的訂單
	public Page<ProductOrderVO> findByFarmerId(Integer farmerId, Pageable pageable);
	
	
}
