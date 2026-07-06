package com.farmily.trip.dto;

import java.sql.Timestamp;

// 小農幫活動開新場次時送進來的資料
// 注意：farmTripId 由網址帶(路徑參數)，不用重複放在 body 裡
// attendance、sessionStatus 都由後端自己設定，不讓前端傳
public class SessionCreateRequest {

    private Timestamp farmTripStart;  // 活動實際開始時間
    private Timestamp farmTripEnd;    // 活動實際結束時間
    private Timestamp tripBookStart;  // 開放報名的開始時間
    private Timestamp tripBookEnd;    // 開放報名的截止時間

    public Timestamp getFarmTripStart() { return farmTripStart; }
    public void setFarmTripStart(Timestamp farmTripStart) { this.farmTripStart = farmTripStart; }

    public Timestamp getFarmTripEnd() { return farmTripEnd; }
    public void setFarmTripEnd(Timestamp farmTripEnd) { this.farmTripEnd = farmTripEnd; }

    public Timestamp getTripBookStart() { return tripBookStart; }
    public void setTripBookStart(Timestamp tripBookStart) { this.tripBookStart = tripBookStart; }

    public Timestamp getTripBookEnd() { return tripBookEnd; }
    public void setTripBookEnd(Timestamp tripBookEnd) { this.tripBookEnd = tripBookEnd; }
}
