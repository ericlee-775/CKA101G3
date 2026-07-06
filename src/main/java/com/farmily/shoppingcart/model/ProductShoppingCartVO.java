package com.farmily.shoppingcart.model;

import com.farmily.coupon.model.CouponDetailId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@IdClass(ProductShoppingCartId.class)
@Table(name="product_shopping_cart")
public class ProductShoppingCartVO {
	@Id
	@Column(name="product_id")
	private Integer productId;
	@Id
	@Column(name="user_id")
	private Integer userId;
	@Column(name="quantity")
	private Integer quantity;
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
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	
	
}
