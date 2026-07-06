package com.farmily.trip.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.farmily.trip.model.FarmTripAudits;

public interface FarmTripAuditsRepository extends JpaRepository<FarmTripAudits, Integer> {
	
	//管理員查詢活動審核狀態
	/** 某活動的審核歷史，最新的排前面 */
    List<FarmTripAudits> findByFarmTripIdOrderByCreatedAtDesc(Integer farmTripId);	
	

}
