package com.farmily.product.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductOrderItemRepository extends JpaRepository<ProductOrderItemVO, Integer> {
	
	// 取得該筆訂單明細
	public Page<ProductOrderItemVO> findByOrder_OrderId(Integer orderId, Pageable pageable);
}
