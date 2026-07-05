package com.farmily.groupbuy.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface GroupBuyOrderRepository extends JpaRepository<GroupBuyOrderVO, Integer> {

	
	
}


