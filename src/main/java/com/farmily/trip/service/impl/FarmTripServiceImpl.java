package com.farmily.trip.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.farmily.trip.repository.FarmTripAuditsRepository;
import com.farmily.trip.repository.FarmTripCommentRepository;
import com.farmily.trip.repository.FarmTripOrderRepository;
import com.farmily.trip.repository.FarmTripRepository;
import com.farmily.trip.repository.FarmTripSessionRepository;
import com.farmily.trip.service.FarmTripService;

@Service
public class FarmTripServiceImpl implements FarmTripService {
	
	private final FarmTripRepository farmTripRepository ;
	private final FarmTripSessionRepository farmTripSessionRepository ;
	private final FarmTripAuditsRepository farmTripAuditsRepository ;
	private final FarmTripOrderRepository farmTripOrderRepository ;
	private final FarmTripCommentRepository farmTripCommentRepository ;
	
	@Autowired
	public FarmTripServiceImpl (FarmTripRepository farmTripRepository,
			FarmTripSessionRepository farmTripSessionRepository,
			FarmTripAuditsRepository farmTripAuditsRepository,
			
			
			
			) {
		this.farmTripService = farmTripService ;
	}
	
	

}
