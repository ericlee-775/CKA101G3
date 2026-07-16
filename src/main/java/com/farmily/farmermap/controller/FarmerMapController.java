package com.farmily.farmermap.controller;

import com.farmily.blog.dto.BlogResponse;
import com.farmily.farmermap.dto.FarmerMapResponse;
import com.farmily.farmermap.service.FarmerMapService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.farmily.trip.dto.TripListResponse;
import com.farmily.trip.service.FarmTripService;

@RestController
@RequestMapping("/api/farms")
public class FarmerMapController {

	private final FarmerMapService farmerMapService;
    private final FarmTripService farmTripService;

    public FarmerMapController(FarmerMapService farmerMapService, FarmTripService farmTripService) {
        this.farmerMapService = farmerMapService;
        this.farmTripService = farmTripService;
    }

    @GetMapping
    public List<FarmerMapResponse> getAllFarms() {
        return farmerMapService.getAllFarms();
    }

    @GetMapping("/{farmerId}")
    public FarmerMapResponse getOneFarm(@PathVariable Integer farmerId) {
        return farmerMapService.getOneFarm(farmerId);
    }

    @GetMapping("/{farmerId}/blogs")
    public List<BlogResponse> getFarmBlogs(@PathVariable Integer farmerId) {
        return farmerMapService.getFarmBlogs(farmerId);
    }
    
 // GET /api/farms/{farmerId}/farm-trips → 該農場上架中的體驗活動（前端農場詳情頁用）
    @GetMapping("/{farmerId}/farm-trips")
    public List<TripListResponse> getFarmTrips(@PathVariable Integer farmerId) {
        return farmTripService.getActiveTripListByFarmer(farmerId);
    }
}
