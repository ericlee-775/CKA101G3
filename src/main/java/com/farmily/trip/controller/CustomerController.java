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

import com.farmily.trip.dto.CommentCreateRequest;
import com.farmily.trip.dto.CommentResponse;
import com.farmily.trip.dto.OrderCreateRequest;
import com.farmily.trip.dto.OrderResponse;
import com.farmily.trip.dto.OrderUpdateRequest;   // 【新增】
import com.farmily.trip.dto.SessionResponse;
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

	// GET /api/farm-trips/3/sessions → 該活動的可報名場次
	@GetMapping("/{farmTripId}/sessions")
	public ResponseEntity<List<SessionResponse>> getSessions(@PathVariable Integer farmTripId) {
		return ResponseEntity.ok(farmTripService.getSessionsByTrip(farmTripId));
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

	// PUT /api/farm-trips/orders/{orderId} → 修改預約（人數、聯絡資訊）
	@PutMapping("/orders/{farmTripOrderId}")
	public ResponseEntity<OrderResponse> updateOrder(@PathVariable Integer farmTripOrderId,
			@RequestBody OrderUpdateRequest request) {
		return ResponseEntity.ok(farmTripService.updateOrder(farmTripOrderId, request));
	}

	// GET /api/farm-trips/3/comments → 評論列表
	@GetMapping("/{farmTripId}/comments")
	public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Integer farmTripId) {
		return ResponseEntity.ok(farmTripService.getComments(farmTripId));
	}

	// POST /api/farm-trips/3/comments → 新增評論／評分
	@PostMapping("/{farmTripId}/comments")
	public ResponseEntity<CommentResponse> addComment(@PathVariable Integer farmTripId,
			@RequestBody CommentCreateRequest request) {
		return ResponseEntity.ok(farmTripService.addComment(farmTripId, request));
	}

}