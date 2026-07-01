package com.farmily.trip.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.farmily.trip.model.FarmTrip;
import com.farmily.trip.service.FarmTripService;

@RestController
@RequestMapping("/api/farm-trips")
public class CostomerController {
	
	private final FarmTripService farmTripService ;
	
	@Autowired
	public FarmTripService (FarmTripService farmTripService) {
		this.farmTripService = farmTripService ;
	}
	
	@GetMapping
    public ResponseEntity<List<FarmTrip>> getAll() {
        return ResponseEntity.ok(farmTripService.getActiveTrips());
    }
	
	
	

}
