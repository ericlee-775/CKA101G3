package com.farmily.notification.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "notification_template")
public class NotificationTemplateVO implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "type_code")
	private String typeCode;
	
	@Column(name = "template_zh")
	private String template;

	
	public String getTypeCode() {
		return typeCode;
	}

	public void setTypeCode(String typeCode) {
		this.typeCode = typeCode;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public NotificationTemplateVO() {
		super();
	}
	
	public NotificationTemplateVO(String typeCode, String template) {
		super();
		this.typeCode = typeCode;
		this.template = template;
	}
	
}
