package com.farmily.trip.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.farmily.trip.model.FarmTripSession;

public interface FarmTripSessionRepository extends JpaRepository<FarmTripSession, Integer> {
	
	//用體驗活動編號查詢活動場次
	List<FarmTripSession> findByFarmTripId(Integer farmTripId);
	

}
