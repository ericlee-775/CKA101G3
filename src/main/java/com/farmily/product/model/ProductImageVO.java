package com.farmily.product.model;

import java.io.IOException;
import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 商品圖片表 product_image。
 * 把圖片 BLOB 從 product_detail 拆出來獨立一張表：
 *   - product_detail 的查詢不會再被圖片 BLOB 拖累
 *   - 一個商品可對多張圖（product_id 為 FK，一對多）
 * product_id 用單純的 Integer 對應 FK（查圖時用 findByProductId 即可，不必載入整個 ProductVO）。
 */
@Entity
@Table(name = "product_image")
public class ProductImageVO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_image_id")
	private Integer productImageId;

	@Column(name = "product_id")
	private Integer productId;          // 對應 product_detail 的 FK

	@JsonIgnore                         // 不要把整包 bytes 序列化進 JSON（圖片走專屬端點輸出）
	@Column(name = "product_image")
	private byte[] productImage;

	public Integer getProductImageId() {
		return productImageId;
	}

	public void setProductImageId(Integer productImageId) {
		this.productImageId = productImageId;
	}

	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
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

}
