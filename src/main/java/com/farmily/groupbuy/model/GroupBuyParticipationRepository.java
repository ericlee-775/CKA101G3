package com.farmily.groupbuy.model;

import org.springframework.data.jpa.repository.JpaRepository;

import com.farmily.user.model.User;

public interface GroupBuyParticipationRepository extends JpaRepository<GroupBuyParticipationVO, Integer> {

	
	boolean existsByGroupBuyIdAndUserId(GroupBuyVO groupBuyId, User userId);
}
