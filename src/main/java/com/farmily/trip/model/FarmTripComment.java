package com.farmily.trip.model;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table(name = "farm_trip_comment") 

public class FarmTripComment {
	
	@Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    @Column(name = "farm_trip_comment", updatable = false)
    private Integer farmTripComment;

    @Column(name = "farm_trip_id")
    private Integer farmTripId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "star")
    private Integer star;
    
    @Column(name = "content")
    private String reason;

    @Column(name = "created_at")
    private Timestamp createdAt;

	public Integer getFarmTripComment() {
		return farmTripComment;
	}

	public void setFarmTripComment(Integer farmTripComment) {
		this.farmTripComment = farmTripComment;
	}

	public Integer getFarmTripId() {
		return farmTripId;
	}

	public void setFarmTripId(Integer farmTripId) {
		this.farmTripId = farmTripId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getStar() {
		return star;
	}

	public void setStar(Integer star) {
		this.star = star;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}
	
	

}
