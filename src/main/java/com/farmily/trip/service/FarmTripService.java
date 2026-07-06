package com.farmily.trip.service;

import java.util.List;

import com.farmily.trip.dto.OrderCreateRequest;
import com.farmily.trip.dto.OrderResponse;
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
	
	OrderResponse bookSession(Integer farmSessionId, OrderCreateRequest request);
	
	List<OrderResponse> getMyOrders(Integer userId);

	OrderResponse cancelOrder(Integer farmTripOrderId);
}