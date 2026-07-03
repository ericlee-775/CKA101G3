package com.farmily.trip.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table(name = "farm_trip_order") 

public class FarmTrip {
	
	@Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    @Column(name = "farm_trip_id", updatable = false)
	private Integer farmTripId;

    @Column(name = "farmer_id")
    private Integer farmerId;

    @Column(name = "farm_trip_type")
    @Enumerated(EnumType.STRING)
    private FarmTripType farmTripType;

    @Column(name = "farm_trip_title")
    private String farmTripTitle;

    @Column(name = "farm_trip_pic")
    private byte[] farmTripPic;

    @Column(name = "farm_trip_intro")
    private String farmTripIntro;

    @Column(name = "location")
    private String location;

    @Column(name = "refer_price")
    private Integer referPrice;

    @Column(name = "trip_status")
    @Enumerated(EnumType.STRING)
    private TripStatus tripStatus;

    @Column(name = "comment_numbers")
    private Integer comentNumbers;

    @Column(name = "star_numbers")
    private Integer starNumbers;

	public Integer getFarmTripId() {
		return farmTripId;
	}

	public void setFarmTripId(Integer farmTripId) {
		this.farmTripId = farmTripId;
	}

	public Integer getFarmerId() {
		return farmerId;
	}

	public void setFarmerId(Integer farmerId) {
		this.farmerId = farmerId;
	}

	public FarmTripType getFarmTripType() {
		return farmTripType;
	}

	public void setFarmTripType(FarmTripType farmTripType) {
		this.farmTripType = farmTripType;
	}

	public String getFarmTripTitle() {
		return farmTripTitle;
	}

	public void setFarmTripTitle(String farmTripTitle) {
		this.farmTripTitle = farmTripTitle;
	}

	public byte[] getFarmTripPic() {
		return farmTripPic;
	}

	public void setFarmTripPic(byte[] farmTripPic) {
		this.farmTripPic = farmTripPic;
	}

	public String getFarmTripIntro() {
		return farmTripIntro;
	}

	public void setFarmTripIntro(String farmTripIntro) {
		this.farmTripIntro = farmTripIntro;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Integer getReferPrice() {
		return referPrice;
	}

	public void setReferPrice(Integer referPrice) {
		this.referPrice = referPrice;
	}

	public TripStatus getTripStatus() {
		return tripStatus;
	}

	public void setTripStatus(TripStatus tripStatus) {
		this.tripStatus = tripStatus;
	}

	public Integer getComentNumbers() {
		return comentNumbers;
	}

	public void setComentNumbers(Integer comentNumbers) {
		this.comentNumbers = comentNumbers;
	}

	public Integer getStarNumbers() {
		return starNumbers;
	}

	public void setStarNumbers(Integer starNumbers) {
		this.starNumbers = starNumbers;
	}

	
    }


  