package com.farmily.trip.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.farmily.trip.model.FarmTripComment;

public interface FarmTripCommentRepository extends JpaRepository<FarmTripComment, Integer> {
	
	List<FarmTripComment> findByFarmTripIdOrderByCreatedAtDesc(Integer farmTripId);

}
