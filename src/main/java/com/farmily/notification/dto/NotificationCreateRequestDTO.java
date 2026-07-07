package com.farmily.notification.dto;

import java.util.HashMap;
import java.util.Map;

import com.farmily.notification.model.NotificationRecipientType;

// DTO, 用於打包要傳進 sendNotif() 的參數
public class NotificationCreateRequestDTO {
	
	
//	public void sendNotif(String typeCode, NotificationRecipientType recipientType, Integer recipientId, 
//							String targetType, Integer targetId, Map<String, String> variables)
	
	private String typeCode; 	// 對應通知文字模板
	private NotificationRecipientType recipientType; 	// 接收者身分
	private Integer recipientId; 	// 接收者 id
	private String targetType;  	// 通知主題 (連結跳轉的目標)
	private Integer targetId;		// 通知主體的 id
	private Map<String, String> variables = new HashMap<>();  // placeholder 所需要的參數對照表
									// 先初始化成一個空的 Map, 後續不用每次檢查是否為 null, 避免 NullPointerException
	
	public NotificationCreateRequestDTO() {
		super();
	}

	public String getTypeCode() {
		return typeCode;
	}

	public void setTypeCode(String typeCode) {
		this.typeCode = typeCode;
	}

	public NotificationRecipientType getRecipientType() {
		return recipientType;
	}

	public void setRecipientType(NotificationRecipientType recipientType) {
		this.recipientType = recipientType;
	}

	public Integer getRecipientId() {
		return recipientId;
	}

	public void setRecipientId(Integer recipientId) {
		this.recipientId = recipientId;
	}

	public String getTargetType() {
		return targetType;
	}

	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}

	public Integer getTargetId() {
		return targetId;
	}

	public void setTargetId(Integer targetId) {
		this.targetId = targetId;
	}

	public Map<String, String> getVariables() {
		return variables;
	}

	public void setVariables(Map<String, String> variables) {
		this.variables = variables;
	}
	

}
