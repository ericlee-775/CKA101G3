package com.farmily.trip.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable; // 【新增】
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.farmily.trip.dto.SessionCreateRequest;
import com.farmily.trip.dto.SessionResponse;
import com.farmily.trip.dto.TripCreateRequest;
import com.farmily.trip.dto.TripDetailResponse;
import com.farmily.trip.service.FarmTripService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.farmily.trip.dto.OrderResponse;
import com.farmily.trip.model.FarmTrip;

@RestController // 掛招牌：我是回傳 JSON 的 Controller
@RequestMapping("/api/farmer/farm-trips") // 掛門牌：我負責這個路徑
public class FarmerTripController {

	private final FarmTripService farmTripService;

	// 跟 Spring 說：把做好的 Service 遞給我（依賴注入）
	@Autowired
	public FarmerTripController(FarmTripService farmTripService) {
		this.farmTripService = farmTripService;
	}

	// POST /api/farmer/farm-trips → 小農建立活動
	@PostMapping
	public ResponseEntity<TripDetailResponse> create(@RequestBody TripCreateRequest request) {
		return ResponseEntity.ok(farmTripService.createTrip(request));
	}

	// POST /api/farmer/farm-trips/{farmTripId}/sessions → 小農幫活動開新場次
	@PostMapping("/{farmTripId}/sessions")
	public ResponseEntity<SessionResponse> createSession(@PathVariable Integer farmTripId,
			@RequestBody SessionCreateRequest request) {
		return ResponseEntity.ok(farmTripService.createSession(farmTripId, request));
	}

	// GET /api/farmer/farm-trips?farmerId=1 → 小農看自己發起的所有活動
	@GetMapping
	public ResponseEntity<List<FarmTrip>> getMyTrips(@RequestParam Integer farmerId) {
		return ResponseEntity.ok(farmTripService.getTripsByFarmer(farmerId));
	}

	// GET /api/farmer/farm-trips/orders?farmerId=1 → 小農看自己活動的所有報名
	@GetMapping("/orders")
	public ResponseEntity<List<OrderResponse>> getFarmerOrders(@RequestParam Integer farmerId) {
		return ResponseEntity.ok(farmTripService.getFarmerOrders(farmerId));
	}

}