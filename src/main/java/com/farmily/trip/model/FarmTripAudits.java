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
@Table(name = "farm_trip_audits") 

public class FarmTripAudits {
	
	@Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    @Column(name = "farm_trip_audits_id", updatable = false)
    private Integer farmTripAuditsId;

    @Column(name = "farm_trip_id")
    private Integer farmTripId;

    @Column(name = "admin_id")
    private Integer adminId;

    @Column(name = "audits_status")
    @Enumerated(EnumType.STRING)
    private AuditsStatus auditsStatus;
    
    @Column(name = "reason")
    private String reason;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "upadted_at")
    private Timestamp updatedAt;

	public Integer getFarmTripAuditsId() {
		return farmTripAuditsId;
	}

	public void setFarmTripAuditsId(Integer farmTripAuditsId) {
		this.farmTripAuditsId = farmTripAuditsId;
	}

	public Integer getFarmTripId() {
		return farmTripId;
	}

	public void setFarmSessionId(Integer farmSessionId) {
		this.farmTripId = farmSessionId;
	}

	public Integer getAdminId() {
		return adminId;
	}

	public void setAdminId(Integer adminId) {
		this.adminId = adminId;
	}

	public AuditsStatus getAuditsStatus() {
		return auditsStatus;
	}

	public void setAuditsStatus(AuditsStatus auditsStatus) {
		this.auditsStatus = auditsStatus;
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

	public Timestamp getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Timestamp updatedAt) {
		this.updatedAt = updatedAt;
	}
 

}
