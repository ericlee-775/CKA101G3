package com.farmily.product.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="maincategory")
public class MainCategoryVO {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="product_main_cat_id")
	private Integer productMainCatId;
	
	@Column(name="product_main_cat_name")
	private String productMainCatName;
}
