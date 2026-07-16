package com.farmily.trip.dto;

// 小農對某場次的報名者發送提醒信時送進來的資料
public class SessionNotifyRequest {

    private String subject;   // 信件主旨（可自訂）
    private String message;   // 信件內文（可自訂，例如衣著、天氣、裝備、集合地點提醒）

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}