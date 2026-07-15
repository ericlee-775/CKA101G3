package com.farmily.product.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductOrderItemRepository extends JpaRepository<ProductOrderItemVO, Integer> {
	
	// 取得該筆訂單明細
	public List<ProductOrderItemVO> findByOrder_OrderIdOrderByOrderItemIdDesc(Integer orderId);

	// 取得屬於該小農的訂單的明細
	public List<ProductOrderItemVO> findByOrder_OrderIdAndFarmerIdOrderByOrderItemIdDesc(Integer orderId, Integer farmerId);
	
}
