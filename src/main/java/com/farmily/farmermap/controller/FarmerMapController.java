package com.farmily.farmermap.controller;

import com.farmily.blog.dto.BlogResponse;
import com.farmily.farmermap.dto.FarmerMapResponse;
import com.farmily.farmermap.service.FarmerMapService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/farms")
public class FarmerMapController {

    private final FarmerMapService farmerMapService;

    public FarmerMapController(FarmerMapService farmerMapService) {
        this.farmerMapService = farmerMapService;
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
}
