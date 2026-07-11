package com.farmily.product.dto;

public class SubCategoryOptionDTO {
	
	public SubCategoryOptionDTO() {
		
	}
	
	public SubCategoryOptionDTO(Integer subCatClassId, String subCatClassName, 
			Integer productMainCatId, String productMainCatName) {
		
		this.subCatClassId = subCatClassId ;
		this.subCatClassName = subCatClassName;
		this.productMainCatId = productMainCatId;
		this.productMainCatName = productMainCatName;
		
	}
		
		private Integer subCatClassId;
		private String  subCatClassName;
		private Integer productMainCatId;
		private String productMainCatName;
		
		public Integer getSubCatClassId() {
			return subCatClassId;
		}

		public void setSubCatClassId(Integer subCatClassId) {
			this.subCatClassId = subCatClassId;
		}

		public String getSubCatClassName() {
			return subCatClassName;
		}

		public void setSubCatClassName(String subCatClassName) {
			this.subCatClassName = subCatClassName;
		}

		public Integer getProductMainCatId() {
			return productMainCatId;
		}

		public void setProductMainCatId(Integer productMainCatId) {
			this.productMainCatId = productMainCatId;
		}

		public String getProductMainCatName() {
			return productMainCatName;
		}

		public void setProductMainCatName(String productMainCatName) {
			this.productMainCatName = productMainCatName;
		}

}
