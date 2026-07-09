package com.farmily.farmermap.service;

import com.farmily.blog.dto.BlogResponse;
import com.farmily.farmermap.dto.FarmerMapResponse;

import java.util.List;

public interface FarmerMapService {

    List<FarmerMapResponse> getAllFarms();

    FarmerMapResponse getOneFarm(Integer farmerId);

    // 某農場的活動端點暫由體驗活動模組負責人日後補上，這裡先不提供
    List<BlogResponse> getFarmBlogs(Integer farmerId);
}
