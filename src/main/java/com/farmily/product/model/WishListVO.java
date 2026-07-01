package com.farmily.product.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@IdClass(WishListId.class)
@Table(name="general_member_product_wishlist")
public class WishListVO implements Serializable {

	@Id
	@Column(name="product_id")
	private Integer productId;
	
	@Id
	@Column(name="user_id")
	private Integer userId;

	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}


	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(productId, userId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WishListVO other = (WishListVO) obj;
		return Objects.equals(productId, other.productId) && Objects.equals(userId, other.userId);
	}
	
	
	
}
