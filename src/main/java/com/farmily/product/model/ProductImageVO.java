package com.farmily.product.model;

import java.beans.ConstructorProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="product_image")
public class ProductImageVO {
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="product_image_id")
	private Integer productImageId;
	
	@Column(name="product_id")
	private Integer productId;
	
	@Column(name="product_image")
	private byte[] productImage;
	
}
