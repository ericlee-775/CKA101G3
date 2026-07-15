package com.farmily.product.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductOrderRepository extends JpaRepository<ProductOrderVO, Integer> {
	
	// 取得該會員所有訂單
	public Page<ProductOrderVO> findByUserId(Integer userId, Pageable pageable);

	// 取得該小農的所有訂單
	@Query ("SELECT o FROM ProductOrderVO o WHERE EXISTS " + 
				"(SELECT 1 FROM ProductOrderItemVO i WHERE i.order = o AND i.farmerId = :farmerId)")
	public Page<ProductOrderVO> findOrdersByFarmerId(@Param ("farmerId") Integer farmerId, Pageable pageable);
}
