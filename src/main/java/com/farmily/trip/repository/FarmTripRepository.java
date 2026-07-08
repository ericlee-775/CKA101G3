package com.farmily.trip.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.farmily.trip.model.FarmTrip;
import com.farmily.trip.model.TripStatus;

public interface FarmTripRepository extends JpaRepository<FarmTrip, Integer>{
	

	//消費者查詢可見活動
	List<FarmTrip> findByTripStatus(TripStatus tripStatus);
	
	//小農查詢活動狀態
	List<FarmTrip> findByFarmerId(Integer farmerId);
	

}
