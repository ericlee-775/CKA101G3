package com.farmily.groupbuy.model;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.farmily.user.model.User;

public interface GroupBuyParticipationRepository extends JpaRepository<GroupBuyParticipationVO, Integer> {
	List<GroupBuyParticipationVO> findByUserId_UserId(Integer userId);
	List<GroupBuyParticipationVO> findByGroupBuyId_GroupBuyIdAndJoinStatus(Integer groupBuyId,JoinStatus joinStatus);
	boolean existsByGroupBuyIdAndUserId(GroupBuyVO groupBuyId, User userId);
	@Query("""
		    SELECT COALESCE(SUM(p.paidAmount), 0)
		    FROM GroupBuyParticipationVO p
		    WHERE p.groupBuyId = :groupBuy
		      AND p.joinStatus = :joinStatus
		""")
		Integer sumPaidAmountByGroupBuy(
		        @Param("groupBuy") GroupBuyVO groupBuy,
		        @Param("joinStatus") JoinStatus joinStatus);
	
	
	@Query("""
		    SELECT COALESCE(SUM(p.buyQty), 0)
		    FROM GroupBuyParticipationVO p
		    WHERE p.groupBuyId = :groupBuy
		      AND p.joinStatus = :joinStatus
		""")
		Integer sumtotalQuantityByGroupBuy(
		        @Param("groupBuy") GroupBuyVO groupBuy,
		        @Param("joinStatus") JoinStatus joinStatus);
	
	
	Optional<GroupBuyParticipationVO>
	findByGroupBuyId_GroupBuyIdAndUserId_UserId(
	        Integer groupBuyId,
	        Integer userId
	);
	
	
	
	@Query(""" 
		    SELECT p.userId.userId
			FROM GroupBuyParticipationVO p
			WHERE p.groupBuyId.groupBuyId = :groupBuyId
			AND p.joinStatus = :joinStatus
			""")
		Set<Integer> findParticipantUserIds(
		        @Param("groupBuyId") Integer groupBuyId,
		        @Param("joinStatus") JoinStatus joinStatus
		);
}
