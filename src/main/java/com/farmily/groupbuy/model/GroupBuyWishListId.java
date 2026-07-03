package com.farmily.groupbuy.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class GroupBuyWishListId implements Serializable {

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "group_buy_id")
    private Integer groupBuyId;

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getGroupBuyId() {
		return groupBuyId;
	}

	public void setGroupBuyId(Integer groupBuyId) {
		this.groupBuyId = groupBuyId;
	}

	public GroupBuyWishListId(Integer userId, Integer groupBuyId) {
		super();
		this.userId = userId;
		this.groupBuyId = groupBuyId;
	}

	public GroupBuyWishListId() {
		super();
	}

	@Override
	public int hashCode() {
		return Objects.hash(groupBuyId, userId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GroupBuyWishListId other = (GroupBuyWishListId) obj;
		return Objects.equals(groupBuyId, other.groupBuyId) && Objects.equals(userId, other.userId);
	}
    
    
}