package com.farmily.groupbuy.model;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="gb_participation")
public class GroupBuyParticipationVO implements java.io.Serializable {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="participation_id",updatable=false)
	private Integer participationId;
	
	@ManyToOne
	@JoinColumn(name="group_buy_id")
	private GroupBuyVO groupBuyId;
	
	@Column(name="user_id")
	private Integer userId;
	
	@Column(name="is_host")
	private boolean isHost;
	
	@Column(name="buy_qty")
	private Integer buyQty;
	
	@Column(name="join_datetime")
	private Timestamp joinDatetime;
	
	@Enumerated(EnumType.STRING)
	@Column(name="join_status")
	private JoinStatus joinStatus;
	
	@Column(name="paid_amount")
	private Integer paidAmount;
	
	@Column(name="paid_datetime")
	private Timestamp paidDatetime;

	public GroupBuyParticipationVO() {
		super();
	}



	public GroupBuyParticipationVO(Integer participationId, GroupBuyVO groupBuyId, Integer userId, boolean isHost,
			Integer buyQty, Timestamp joinDatetime, JoinStatus joinStatus, Integer paidAmount, Timestamp paidDatetime) {
		super();
		this.participationId = participationId;
		this.groupBuyId = groupBuyId;
		this.userId = userId;
		this.isHost = isHost;
		this.buyQty = buyQty;
		this.joinDatetime = joinDatetime;
		this.joinStatus = joinStatus;
		this.paidAmount = paidAmount;
		this.paidDatetime = paidDatetime;
	}



	public GroupBuyVO getGroupBuyId() {
		return groupBuyId;
	}



	public void setGroupBuyId(GroupBuyVO groupBuyId) {
		this.groupBuyId = groupBuyId;
	}



	public Integer getParticipationId() {
		return participationId;
	}

	public void setParticipationId(Integer participationId) {
		this.participationId = participationId;
	}



	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public boolean isHost() {
		return isHost;
	}

	public void setHost(boolean isHost) {
		this.isHost = isHost;
	}

	public Integer getBuyQty() {
		return buyQty;
	}

	public void setBuyQty(Integer buyQty) {
		this.buyQty = buyQty;
	}

	public Timestamp getJoinDatetime() {
		return joinDatetime;
	}

	public void setJoinDatetime(Timestamp joinDatetime) {
		this.joinDatetime = joinDatetime;
	}

	public JoinStatus getJoinStatus() {
		return joinStatus;
	}

	public void setJoinStatus(JoinStatus joinStatus) {
		this.joinStatus = joinStatus;
	}

	public Integer getPaidAmount() {
		return paidAmount;
	}

	public void setPaidAmount(Integer paidAmount) {
		this.paidAmount = paidAmount;
	}

	public Timestamp getPaidDatetime() {
		return paidDatetime;
	}

	public void setPaidDatetime(Timestamp paidDatetime) {
		this.paidDatetime = paidDatetime;
	}
	
	
	

}
