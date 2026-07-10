package com.farmily.trip.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;                     // 【新增】
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;  // 【新增】
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import com.farmily.trip.dto.OrderResponse;
import com.farmily.trip.dto.SessionCreateRequest;
import com.farmily.trip.dto.SessionResponse;
import com.farmily.trip.dto.TripCreateRequest;
import com.farmily.trip.dto.TripDetailResponse;
import com.farmily.trip.model.FarmTrip;
import com.farmily.trip.service.FarmTripService;

@RestController
@RequestMapping("/api/farmer/farm-trips")
public class FarmerTripController {

	private final FarmTripService farmTripService;

	@Autowired
	public FarmerTripController(FarmTripService farmTripService) {
		this.farmTripService = farmTripService;
	}

	// POST /api/farmer/farm-trips → 小農建立活動（含上傳圖片，multipart/form-data）
	// 用 @ModelAttribute 把表單欄位 + 檔案一起綁進 TripCreateRequest（pic 是 MultipartFile）
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<TripDetailResponse> create(@ModelAttribute TripCreateRequest request) {
		return ResponseEntity.ok(farmTripService.createTrip(request));
	}

	// POST /api/farmer/farm-trips/{farmTripId}/sessions → 開新場次
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