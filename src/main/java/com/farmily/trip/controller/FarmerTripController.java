package com.farmily.trip.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType; // 【新增】
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute; // 【新增】
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

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import com.farmily.trip.dto.SessionNotifyRequest;
import com.farmily.trip.dto.TripUpdateRequest;

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

	// PUT /api/farmer/farm-trips/{farmTripId} → 小農修改活動（multipart，可含新圖片），改後回待審核
	@PutMapping(value = "/{farmTripId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<TripDetailResponse> update(@PathVariable Integer farmTripId,
			@ModelAttribute TripUpdateRequest request) {
		return ResponseEntity.ok(farmTripService.updateTrip(farmTripId, request));
	}

	// DELETE /api/farmer/farm-trips/{farmTripId}?farmerId=1 → 小農刪除活動（已有人報名則擋下）
	@DeleteMapping("/{farmTripId}")
	public ResponseEntity<Void> delete(@PathVariable Integer farmTripId, @RequestParam Integer farmerId) {
		farmTripService.deleteTrip(farmTripId, farmerId);
		return ResponseEntity.noContent().build();
	}

	// PUT /api/farmer/farm-trips/sessions/{farmSessionId} → 修改場次時間
	@PutMapping("/sessions/{farmSessionId}")
	public ResponseEntity<SessionResponse> updateSession(@PathVariable Integer farmSessionId,
			@RequestBody SessionCreateRequest request) {
		return ResponseEntity.ok(farmTripService.updateSession(farmSessionId, request));
	}

	// PUT /api/farmer/farm-trips/sessions/{farmSessionId}/cancel → 取消場次（連帶取消報名並通知）
	@PutMapping("/sessions/{farmSessionId}/cancel")
	public ResponseEntity<SessionResponse> cancelSession(@PathVariable Integer farmSessionId) {
		return ResponseEntity.ok(farmTripService.cancelSession(farmSessionId));
	}

	// PUT /api/farmer/farm-trips/sessions/{farmSessionId}/reopen → 重啟已取消的場次
	@PutMapping("/sessions/{farmSessionId}/reopen")
	public ResponseEntity<SessionResponse> reopenSession(@PathVariable Integer farmSessionId) {
		return ResponseEntity.ok(farmTripService.reopenSession(farmSessionId));
	}

	// POST /api/farmer/farm-trips/sessions/{farmSessionId}/notify → 寄提醒信給該場次未取消的報名者
	@PostMapping("/sessions/{farmSessionId}/notify")
	public ResponseEntity<Integer> notifyRegistrants(@PathVariable Integer farmSessionId,
			@RequestBody SessionNotifyRequest request) {
		return ResponseEntity.ok(farmTripService.notifySessionRegistrants(farmSessionId, request));
	}
}