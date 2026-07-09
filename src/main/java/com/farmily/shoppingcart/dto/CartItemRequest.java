package com.farmily.shoppingcart.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CartItemRequest {

	@NotNull(message = "數量不可為空")
	@Min(value=1,message="數量至少為1")
	@Max(value=999,message="數量過大")
	private Integer quantity;

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	
	
}
