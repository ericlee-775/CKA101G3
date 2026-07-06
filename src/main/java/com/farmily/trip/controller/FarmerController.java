package com.farmily.trip.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.farmily.trip.dto.TripCreateRequest;
import com.farmily.trip.dto.TripDetailResponse;
import com.farmily.trip.service.FarmTripService;

@RestController                              // 掛招牌：我是回傳 JSON 的 Controller
@RequestMapping("/api/farmer/farm-trips")    // 掛門牌：我負責這個路徑
public class FarmerController {

	private final FarmTripService farmTripService;

	// 跟 Spring 說：把做好的 Service 遞給我（依賴注入）
	@Autowired
	public FarmerController(FarmTripService farmTripService) {
		this.farmTripService = farmTripService;
	}

	// POST /api/farmer/farm-trips → 小農建立活動
	@PostMapping
	public ResponseEntity<TripDetailResponse> create(@RequestBody TripCreateRequest request) {
		return ResponseEntity.ok(farmTripService.createTrip(request));
	}

}