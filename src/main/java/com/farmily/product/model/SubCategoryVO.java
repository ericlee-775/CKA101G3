package com.farmily.product.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="subcategory")
public class SubCategoryVO {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="sub_cat_class_id)")
	private Integer subCatClassId;
	
	@Column(name="product_main_cat_id")
	private Integer productMainCatId;
	
	@Column(name="sub_cat_class_name")
	private String subCatClassName;
}
