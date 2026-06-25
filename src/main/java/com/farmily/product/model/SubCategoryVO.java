package com.farmily.product.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="subcategory")
public class SubCategoryVO {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="sub_cat_class_id")
	private Integer subCatClassId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="product_main_cat_id",referencedColumnName ="product_main_cat_id")
	private MainCategoryVO mainCategoryVO;
	
	@Column(name="sub_cat_class_name")
	private String subCatClassName;

	public Integer getSubCatClassId() {
		return subCatClassId;
	}

	public void setSubCatClassId(Integer subCatClassId) {
		this.subCatClassId = subCatClassId;
	}

	public MainCategoryVO getMainCategoryVO() {
		return mainCategoryVO;
	}

	public void setMainCategoryVO(MainCategoryVO mainCategoryVO) {
		this.mainCategoryVO = mainCategoryVO;
	}

	public String getSubCatClassName() {
		return subCatClassName;
	}

	public void setSubCatClassName(String subCatClassName) {
		this.subCatClassName = subCatClassName;
	}
	
	
}
