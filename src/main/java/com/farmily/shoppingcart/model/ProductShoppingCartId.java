package com.farmily.shoppingcart.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Id;

public class ProductShoppingCartId implements Serializable{
	private Integer productId;
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
		ProductShoppingCartId other = (ProductShoppingCartId) obj;
		return Objects.equals(productId, other.productId) && Objects.equals(userId, other.userId);
	}
	
	
}
