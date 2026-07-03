package com.farmily.groupbuy.model;

import java.sql.Timestamp;

import com.farmily.user.model.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name="gb_wishlist")
public class GroupBuyWishListVO {
	
	
	@JoinColumn(name="user_id")
	private User userId;
	
	@JoinColumn(name="group_buy_id")
	private GroupBuyVO groupBuyId;
	
	@Column(name="saved_datetime")
	private Timestamp savedDatetime;
}
