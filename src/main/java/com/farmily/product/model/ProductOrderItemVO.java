package com.farmily.product.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table (name = "order_item")
public class ProductOrderItemVO implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column (name = "order_item_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer orderItemId;
	
	@Column (name = "product_id")
	private Integer productId;
	
	@Column (name = "product_name")
	private String productName;
	
	@Column (name = "farmer_id")
	private Integer farmerId;
		
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "order_id", referencedColumnName = "order_id")
	private ProductOrderVO order;
	
	@Column (name = "quantity")
	private Integer quantity;
	
	@Column (name = "price")
	private Integer price;
	
	@Column (name = "shipped_status")
	@Enumerated (EnumType.STRING)
	private ShippedStatus shippedStatus;
	
	@Column (name = "shipped_at")
	private LocalDateTime shippedAt;
	
	@Column (name = "received_at")
	private LocalDateTime receivedAt;

	@Column (name = "payout_status")
	@Enumerated (EnumType.STRING)
	private PayoutStatus payoutStatus;
	
	
	public ProductOrderItemVO() {
		super();
	}

	public Integer getOrderItemId() {
		return orderItemId;
	}

	public void setOrderItemId(Integer orderItemId) {
		this.orderItemId = orderItemId;
	}

	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}
	
	public Integer getFarmerId() {
		return farmerId;
	}
	
	public void setFarmerId(Integer farmerId) {
		this.farmerId = farmerId;
	}

	public ProductOrderVO getOrder() {
		return order;
	}

	public void setOrder(ProductOrderVO order) {
		this.order = order;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Integer getPrice() {
		return price;
	}

	public void setPrice(Integer price) {
		this.price = price;
	}

	public ShippedStatus getShippedStatus() {
		return shippedStatus;
	}

	public void setShippedStatus(ShippedStatus shippedStatus) {
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

	public PayoutStatus getPayoutStatus() {
		return payoutStatus;
	}

	public void setPayoutStatus(PayoutStatus payoutStatus) {
		this.payoutStatus = payoutStatus;
	}
	
	@PrePersist
	protected void onCreate() {
		if (shippedStatus == null) {
			shippedStatus = ShippedStatus.pending;
		}
		if (payoutStatus == null) {
			payoutStatus = PayoutStatus.pending;
		}
	}
}
