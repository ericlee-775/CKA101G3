package com.farmily.trip.dto;

import java.sql.Timestamp;

// 回傳給前端看的場次資料(消費者選場次、小農管理場次都共用這個)
public class SessionResponse {

    private Integer farmSessionId;
    private Integer farmTripId;
    private Timestamp farmTripStart;
    private Timestamp farmTripEnd;
    private Timestamp tripBookStart;
    private Timestamp tripBookEnd;
    private Integer attendance;     // 目前已報名人數(還沒有上限欄位，前端先只顯示這個)
    private String sessionStatus;   // enum 轉字串回傳，跟其他 DTO 一致

    public Integer getFarmSessionId() { return farmSessionId; }
    public void setFarmSessionId(Integer farmSessionId) { this.farmSessionId = farmSessionId; }

    public Integer getFarmTripId() { return farmTripId; }
    public void setFarmTripId(Integer farmTripId) { this.farmTripId = farmTripId; }

    public Timestamp getFarmTripStart() { return farmTripStart; }
    public void setFarmTripStart(Timestamp farmTripStart) { this.farmTripStart = farmTripStart; }

    public Timestamp getFarmTripEnd() { return farmTripEnd; }
    public void setFarmTripEnd(Timestamp farmTripEnd) { this.farmTripEnd = farmTripEnd; }

    public Timestamp getTripBookStart() { return tripBookStart; }
    public void setTripBookStart(Timestamp tripBookStart) { this.tripBookStart = tripBookStart; }

    public Timestamp getTripBookEnd() { return tripBookEnd; }
    public void setTripBookEnd(Timestamp tripBookEnd) { this.tripBookEnd = tripBookEnd; }

    public Integer getAttendance() { return attendance; }
    public void setAttendance(Integer attendance) { this.attendance = attendance; }

    public String getSessionStatus() { return sessionStatus; }
    public void setSessionStatus(String sessionStatus) { this.sessionStatus = sessionStatus; }
}
