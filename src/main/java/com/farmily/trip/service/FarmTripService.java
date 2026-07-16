package com.farmily.trip.service;

import java.util.List;

import com.farmily.trip.dto.CommentCreateRequest;
import com.farmily.trip.dto.CommentResponse;
import com.farmily.trip.dto.OrderCreateRequest;
import com.farmily.trip.dto.OrderResponse;
import com.farmily.trip.dto.OrderUpdateRequest;   
import com.farmily.trip.dto.SessionCreateRequest;
import com.farmily.trip.dto.SessionResponse;
import com.farmily.trip.dto.TripCreateRequest;
import com.farmily.trip.dto.TripDetailResponse;
import com.farmily.trip.dto.TripListResponse;
import com.farmily.trip.dto.TripReviewRequest;
import com.farmily.trip.model.FarmTrip;
import com.farmily.trip.dto.SessionNotifyRequest;
import com.farmily.trip.dto.TripUpdateRequest;

public interface FarmTripService {

	List<FarmTrip> getActiveTrips();

	List<TripListResponse> getActiveTripList();

	TripDetailResponse getTripDetail(Integer farmTripId);

	TripDetailResponse createTrip(TripCreateRequest request);

	TripDetailResponse reviewTrip(Integer farmTripId, TripReviewRequest request);

	List<SessionResponse> getSessionsByTrip(Integer farmTripId);

	SessionResponse createSession(Integer farmTripId, SessionCreateRequest request);

	OrderResponse bookSession(Integer farmSessionId, OrderCreateRequest request);

	List<OrderResponse> getMyOrders(Integer userId);

	OrderResponse cancelOrder(Integer farmTripOrderId);

	List<CommentResponse> getComments(Integer farmTripId);

	CommentResponse addComment(Integer farmTripId, CommentCreateRequest request);

	OrderResponse updateOrder(Integer farmTripOrderId, OrderUpdateRequest request);

	List<FarmTrip> getTripsByFarmer(Integer farmerId);

	List<OrderResponse> getFarmerOrders(Integer farmerId);

	byte[] getTripImage(Integer farmTripId);
	
	// 管理後台：列出待審核的活動
	List<FarmTrip> getPendingTrips();
		
	// 小農修改場次時間
	SessionResponse updateSession(Integer farmSessionId, SessionCreateRequest request);

	// 小農取消場次（連帶取消該場次報名並 email 通知報名者）
	SessionResponse cancelSession(Integer farmSessionId);

	// 小農主動寄提醒信給該場次「未取消」的報名者，回傳實際寄出的人數
	int notifySessionRegistrants(Integer farmSessionId, SessionNotifyRequest request);

	// 小農修改自己的活動（修改後改回 PENDING 重新送審）
	TripDetailResponse updateTrip(Integer farmTripId, TripUpdateRequest request);

	// 小農刪除自己的活動（已有人報名則禁止）
	void deleteTrip(Integer farmTripId, Integer farmerId);

}