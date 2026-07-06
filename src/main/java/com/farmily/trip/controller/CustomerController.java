package com.farmily.trip.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.farmily.trip.dto.OrderCreateRequest;
import com.farmily.trip.dto.OrderResponse;
import com.farmily.trip.dto.TripDetailResponse;
import com.farmily.trip.model.FarmTrip;
import com.farmily.trip.service.FarmTripService;

@RestController
@RequestMapping("/api/farm-trips")
public class CustomerController {

	private final FarmTripService farmTripService;

	@Autowired
	public CustomerController(FarmTripService farmTripService) {
		this.farmTripService = farmTripService;
	}

	@GetMapping
	public ResponseEntity<List<FarmTrip>> getAll() {
		return ResponseEntity.ok(farmTripService.getActiveTrips());
	}

	// GET /api/farm-trips/3 → 看 id 為 3 的活動詳情
	@GetMapping("/{farmTripId}")
	public ResponseEntity<TripDetailResponse> getDetail(@PathVariable Integer farmTripId) {
		return ResponseEntity.ok(farmTripService.getTripDetail(farmTripId));
	}

	// POST /api/farm-trips/sessions/{farmSessionId}/orders → 報名場次
	@PostMapping("/sessions/{farmSessionId}/orders")
	public ResponseEntity<OrderResponse> bookSession(@PathVariable Integer farmSessionId,
			@RequestBody OrderCreateRequest request) {
		return ResponseEntity.ok(farmTripService.bookSession(farmSessionId, request));
	}

	// GET /api/farm-trips/orders/mine?userId=1 → 我的報名清單
	@GetMapping("/orders/mine")
	public ResponseEntity<List<OrderResponse>> getMyOrders(@RequestParam Integer userId) {
		return ResponseEntity.ok(farmTripService.getMyOrders(userId));
	}

	// PUT /api/farm-trips/orders/{orderId}/cancel → 取消報名
	@PutMapping("/orders/{farmTripOrderId}/cancel")
	public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Integer farmTripOrderId) {
		return ResponseEntity.ok(farmTripService.cancelOrder(farmTripOrderId));
	}

}
