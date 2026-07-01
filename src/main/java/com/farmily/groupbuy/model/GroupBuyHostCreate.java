package com.farmily.groupbuy.model;

import java.io.Serializable;
import java.sql.Timestamp;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class GroupBuyHostCreate implements Serializable {
	private static final long serialVersionUID = 1L;

	@NotNull(message="請輸入達標金額")
	@Min(1)
	private Integer targetAmount;

	@NotNull(message="請輸入開團時間")
	private Timestamp openDatetime;

	@NotNull(message="請輸入截止時間")
	private Timestamp ddlDatetime;

	@NotBlank(message="請輸入收貨地址")
	private String pickupAddress;

	public Integer getTargetAmount() {
		return targetAmount;
	}

	public void setTargetAmount(Integer targetAmount) {
		this.targetAmount = targetAmount;
	}

	public Timestamp getOpenDatetime() {
		return openDatetime;
	}

	public void setOpenDatetime(Timestamp openDatetime) {
		this.openDatetime = openDatetime;
	}

	public Timestamp getDdlDatetime() {
		return ddlDatetime;
	}

	public void setDdlDatetime(Timestamp ddlDatetime) {
		this.ddlDatetime = ddlDatetime;
	}

	public String getPickupAddress() {
		return pickupAddress;
	}

	public void setPickupAddress(String pickupAddress) {
		this.pickupAddress = pickupAddress;
	}

}
