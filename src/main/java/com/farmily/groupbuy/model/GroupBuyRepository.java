
package com.farmily.groupbuy.model;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface GroupBuyRepository extends JpaRepository<GroupBuyVO, Integer> {
	List<GroupBuyVO> findByProduct_FarmerId(Integer farmerId);
	List<GroupBuyVO> findByRequestStatus(RequestStatus requestStatus,GroupBuyStatus groupBuyStatus);
	Optional<GroupBuyVO> findByGroupBuyIdAndProduct_FarmerId(Integer groupBuyId, Integer farmerId);
	
	List<GroupBuyVO> findByRequestStatusAndStatusIn(RequestStatus requestStatus,List<GroupBuyStatus> statuses);
	List<GroupBuyVO> findByStatusAndDdlDatetimeBefore(GroupBuyStatus status,Timestamp now);
}