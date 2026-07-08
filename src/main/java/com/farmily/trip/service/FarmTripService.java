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

}