package com.farmily.groupbuy.model;

import java.sql.Timestamp;

import com.farmily.user.model.User;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "gb_wishlist")
public class GroupBuyWishListVO {

	@EmbeddedId
	private GroupBuyWishListId id;

	@ManyToOne
	@MapsId("userId")
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne
	@MapsId("groupBuyId")
	@JoinColumn(name = "group_buy_id")
	private GroupBuyVO groupBuy;

	@Column(name = "saved_datetime")
	private Timestamp savedDatetime;

	public GroupBuyWishListVO() {
	}

	public GroupBuyWishListId getId() {
		return id;
	}

	public void setId(GroupBuyWishListId id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public GroupBuyVO getGroupBuy() {
		return groupBuy;
	}

	public void setGroupBuy(GroupBuyVO groupBuy) {
		this.groupBuy = groupBuy;
	}

	public Timestamp getSavedDatetime() {
		return savedDatetime;
	}

	public void setSavedDatetime(Timestamp savedDatetime) {
		this.savedDatetime = savedDatetime;
	}
	
}
