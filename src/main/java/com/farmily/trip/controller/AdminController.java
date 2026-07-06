package com.farmily.trip.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.farmily.trip.dto.TripDetailResponse;
import com.farmily.trip.dto.TripReviewRequest;
import com.farmily.trip.service.FarmTripService;

@RestController
@RequestMapping("/api/admin/farm-trips")
public class AdminController {

	private final FarmTripService farmTripService;

	@Autowired
	public AdminController(FarmTripService farmTripService) {
		this.farmTripService = farmTripService;
	}

	// PUT /api/admin/farm-trips/{id}/review → 審核活動
	@PutMapping("/{farmTripId}/review")
	public ResponseEntity<TripDetailResponse> review(@PathVariable Integer farmTripId,
			@RequestBody TripReviewRequest request) {
		return ResponseEntity.ok(farmTripService.reviewTrip(farmTripId, request));
	}

}