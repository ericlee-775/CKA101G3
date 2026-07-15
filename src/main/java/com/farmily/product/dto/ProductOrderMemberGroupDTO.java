package com.farmily.product.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ProductOrderMemberGroupDTO {
	
		private Integer farmerId;
		private String farmerName;
		private String shippedStatus;
		private LocalDateTime shippedAt;
		private LocalDateTime receivedAt;
		private Integer subtotal; 		// 該農場商品小計
		private List<ProductOrderItemResponseDTO> items;
		
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
		
		public String getShippedStatus() {
			return shippedStatus;
		}
		
		public void setShippedStatus(String shippedStatus) {
			this.shippedStatus = shippedStatus;
		}
		
		public LocalDateTime getShippedAt() {
			return shippedAt;
		}
		
		public void setShippedAt(LocalDateTime shippedAt) {
			this.shippedAt = shippedAt;
		}
		
		public LocalDateTime getReceivedAt() {
			return receivedAt;
		}
		
		public void setReceivedAt(LocalDateTime receivedAt) {
			this.receivedAt = receivedAt;
		}
		
		public Integer getSubtotal() {
			return subtotal;
		}
		
		public void setSubtotal(Integer subtotal) {
			this.subtotal = subtotal;
		}
		
		public List<ProductOrderItemResponseDTO> getItems() {
			return items;
		}
		
		public void setItems(List<ProductOrderItemResponseDTO> items) {
			this.items = items;
		}

}
