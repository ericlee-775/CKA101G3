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
	@Table(name = "farm_trip_session") 

	public class FarmTripSession {
		
		@Id 
	    @GeneratedValue(strategy = GenerationType.IDENTITY) 
	    @Column(name = "farm_session_id", updatable = false)
		private Integer farmSessionId;

	    @Column(name = "farm_trip_id")
	    private Integer farmTripId;

	    @Column(name = "farm_trip_start")
	    private Timestamp farmTripStart;

	    @Column(name = "farm_trip_end")
	    private Timestamp farmTripEnd;

	    @Column(name = "trip_book_start")
	    private Timestamp tripBookStart;

	    @Column(name = "trip_book_end")
	    private Timestamp tripBookEnd;

	    @Column(name = "attendance")
	    private Integer attendance;

	    @Column(name = "session_status")
	    @Enumerated(EnumType.STRING)
	    private SessionStatus sessionStatus;

		public Integer getFarmSessionId() {
			return farmSessionId;
		}

		public void setFarmSessionId(Integer farmSessionId) {
			this.farmSessionId = farmSessionId;
		}

		public Integer getFarmerTripId() {
			return farmTripId;
		}

		public void setFarmerTripId(Integer farmerTripId) {
			this.farmTripId = farmerTripId;
		}

		public Timestamp getFarmTripStart() {
			return farmTripStart;
		}

		public void setFarmTripStart(Timestamp farmTripStart) {
			this.farmTripStart = farmTripStart;
		}

		public Timestamp getFarmTripEnd() {
			return farmTripEnd;
		}

		public void setFarmTripEnd(Timestamp farmTripEnd) {
			this.farmTripEnd = farmTripEnd;
		}

		public Timestamp getTripBookStart() {
			return tripBookStart;
		}

		public void setTripBookStart(Timestamp tripBookStart) {
			this.tripBookStart = tripBookStart;
		}

		public Timestamp getTripBookEnd() {
			return tripBookEnd;
		}

		public void setTripBookEnd(Timestamp tripBookEnd) {
			this.tripBookEnd = tripBookEnd;
		}

		public Integer getAttendance() {
			return attendance;
		}

		public void setAttendance(Integer attendance) {
			this.attendance = attendance;
		}

		public SessionStatus getSessionStatus() {
			return sessionStatus;
		}

		public void setSessionStatus(SessionStatus sessionStatus) {
			this.sessionStatus = sessionStatus;
		}

}
