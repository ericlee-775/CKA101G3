package com.farmily.product.dto;

import java.util.List;

// 用於前端顯示 checkout-info 時訂單下按小農分組顯示
public class ProductOrderFarmerGroupDTO {
	
	private Integer farmerId;
	private String farmerName;
	private List<ProductOrderCheckoutItemDTO> items; 	// 商品列表
	private Integer subtotal; 	// 該農場小計
	
	public Integer getFarmerId() {
		return farmerId;
	}
	public void setFarmerId(Integer farmerId) {
		this.farmerId = farmerId;
	}
	public String getFarmerName() {
		return farmerName;
	}
	public void setFarmerName(String farmerName) {
		this.farmerName = farmerName;
	}
	public List<ProductOrderCheckoutItemDTO> getItems() {
		return items;
	}
	public void setItems(List<ProductOrderCheckoutItemDTO> items) {
		this.items = items;
	}
	public Integer getSubtotal() {
		return subtotal;
	}
	public void setSubtotal(Integer subtotal) {
		this.subtotal = subtotal;
	}
	
	
}
