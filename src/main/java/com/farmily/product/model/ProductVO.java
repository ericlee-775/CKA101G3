package com.farmily.product.model;

import java.io.IOException;
import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import jakarta.persistence.Table;

//@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name="product_detail")
public class ProductVO implements Serializable{
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="product_id")
	private Integer productId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="sub_cat_class_id",referencedColumnName = "sub_cat_class_id")
	private SubCategoryVO subCategoryVO;
	
	@Column(name="farmer_id")
	private Integer farmerId;
	
	@Column(name="retail_price")
	private Integer retailPrice;
	
	@Column(name="group_price")
	private Integer groupPrice;
	
	@Column(name="unit_pricing_measure")
	private String unitPricingMeasure;
	
	
	@JsonIgnore
	@Column(name="product_image")
	private byte[] productImage;
	
	@Column(name="is_group_buy")
	private Boolean isGroupBuy;
	
	@Column(name="description")
	private String description;
	
	@Column(name="status")
	@Enumerated(EnumType.STRING)
	private Status status;
	
	@Column(name="product_name")
	private String productName;
	
	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}
	
	public SubCategoryVO getSubCategoryVO() {
		return subCategoryVO;
	}

	public void setSubCategoryVO(SubCategoryVO subCategoryVO) {
		this.subCategoryVO = subCategoryVO;
	}

	public Integer getFarmerId() {
		return farmerId;
	}

	public void setFarmerId(Integer farmerId) {
		this.farmerId = farmerId;
	}

	public Integer getRetailPrice() {
		return retailPrice;
	}

	public void setRetailPrice(Integer retailPrice) {
		this.retailPrice = retailPrice;
	}

	public Integer getGroupPrice() {
		return groupPrice;
	}

	public void setGroupPrice(Integer groupPrice) {
		this.groupPrice = groupPrice;
	}

	public String getUnitPricingMeasure() {
		return unitPricingMeasure;
	}

	public void setUnitPricingMeasure(String unitPricingMeasure) {
		this.unitPricingMeasure = unitPricingMeasure;
	}

	public byte[] getProductImage() {
		return productImage;
	}

	public void setProductImage(byte[] productImage) {
		this.productImage = productImage;
	}
	
	// 前端 multipart 的檔案會進到這個 setter，Spring 自動呼叫
    public void setProductImage(MultipartFile multipartFile) {
        try {
            this.productImage = multipartFile.getBytes();   // 檔案 → byte[]
        } catch (IOException e) {
            throw new RuntimeException("圖片讀取失敗", e);
        }
    }

	public Boolean getIsGroupBuy() {
		return isGroupBuy;
	}

	public void setIsGroupBuy(Boolean isGroupBuy) {
		this.isGroupBuy = isGroupBuy;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}
	
	
}
