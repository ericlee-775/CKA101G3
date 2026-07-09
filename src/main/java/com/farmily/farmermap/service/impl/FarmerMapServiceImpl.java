package com.farmily.farmermap.service.impl;

import com.farmily.blog.constant.BlogStatus;
import com.farmily.blog.dto.BlogResponse;
import com.farmily.blog.model.Blog;
import com.farmily.blog.repository.BlogRepository;
import com.farmily.farmermap.dto.FarmerMapResponse;
import com.farmily.farmermap.repository.FarmerMapRepository;
import com.farmily.farmermap.service.FarmerMapService;
import com.farmily.user.model.Farmer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class FarmerMapServiceImpl implements FarmerMapService {

    private final FarmerMapRepository farmerMapRepository;
    private final BlogRepository blogRepository;

    public FarmerMapServiceImpl(FarmerMapRepository farmerMapRepository,
                                BlogRepository blogRepository) {
        this.farmerMapRepository = farmerMapRepository;
        this.blogRepository = blogRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FarmerMapResponse> getAllFarms() {
        List<Farmer> farmers = farmerMapRepository.findByFarmerStatusOrderByFarmerIdAsc(Farmer.FarmerStatus.ACTIVE);
        List<FarmerMapResponse> responses = new ArrayList<>();

        for (Farmer farmer : farmers) {
            responses.add(FarmerMapResponse.fromListItem(farmer));   // 清單不回電話，避免公開端點整包外洩 PII
        }

        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public FarmerMapResponse getOneFarm(Integer farmerId) {
        return FarmerMapResponse.from(findActiveFarmer(farmerId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogResponse> getFarmBlogs(Integer farmerId) {
        findActiveFarmer(farmerId);
        List<Blog> blogs = blogRepository.findByFarmerIdAndBlogStatusOrderByBlogTimeDesc(
                farmerId, BlogStatus.VISIBLE);
        List<BlogResponse> responses = new ArrayList<>();

        for (Blog blog : blogs) {
            responses.add(BlogResponse.from(blog));
        }

        return responses;
    }

    private Farmer findActiveFarmer(Integer farmerId) {
        return farmerMapRepository.findByFarmerIdAndFarmerStatus(farmerId, Farmer.FarmerStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "查無此農場: " + farmerId));
    }
}
