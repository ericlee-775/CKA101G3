package com.farmily.groupbuy.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

@Component
public interface GroupBuyOrderRepository extends JpaRepository<GroupBuyOrderVO, Integer> {
	List<GroupBuyOrderVO> findByGroupBuyId_Product_FarmerId(Integer farmerId);
	//給小農看待出貨跟已出貨
	List<GroupBuyOrderVO>findByShippedStatusAndOrderStatus(ShippedStatus shippedStatus,OrderStatus orderstatus);
	Optional<GroupBuyOrderVO>findByOrderIdAndGroupBuyId_Product_FarmerId(Integer OrderId,Integer farmerId);
	Optional<GroupBuyOrderVO>findByOrderIdAndGroupBuyId_HostUser_UserId(Integer orderId,Integer userId);
	
	
	@Query("""
		    SELECT o
		    FROM GroupBuyOrderVO o
		    JOIN GroupBuyParticipationVO p
		      ON p.groupBuyId = o.groupBuyId
		    WHERE p.userId.userId = :userId
		      AND p.joinStatus = :joinStatus
		      AND o.groupBuyId.status = :status
		""")
		List<GroupBuyOrderVO> findMySuccessOrders(
		        @Param("userId") Integer userId,
		        @Param("joinStatus") JoinStatus joinStatus,
		        @Param("status") GroupBuyStatus status
		);
	
}


