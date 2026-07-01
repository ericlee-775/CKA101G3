package com.farmily.trip.service;

import java.util.List;

import com.farmily.trip.model.FarmTrip;

public interface FarmTripService {
	
	List<FarmTrip> getActiveTrips();
	
	FarmTrip getTripById(Integer tripId);
	
	List<FarmTrip> getTripsByFarmer(Integer farmerId);
	
	FarmTrip createTrip(FarmTrip trip);
	
	FarmTrip updateTrip(Integer tripId, FarmTrip trip);

}
