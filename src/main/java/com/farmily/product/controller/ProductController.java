package com.farmily.product.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLConnection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.farmily.product.dto.ProductDetailDTO;
import com.farmily.product.dto.ProductInsertDTO;
import com.farmily.product.dto.ProductManageDTO;
import com.farmily.product.dto.ProductSummeryDTO;
import com.farmily.product.dto.ProductUpdatedDTO;
import com.farmily.product.service.ProductService;
import com.farmily.user.security.FarmerUserDetails;
import com.farmily.user.security.MemberUserDetails;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
public class ProductController {

	@Autowired
	private ProductService productService;

	// 查詢所有商品（統一回傳 DTO，對應前端 Vue 串接）
	@GetMapping
	public ResponseEntity<List<ProductSummeryDTO>> getAllProducts() {
		List<ProductSummeryDTO> products = productService.getAllProducts();
		return ResponseEntity.ok(products);
	}

	// 查詢單一商品詳情（給商品詳情頁）；查無回 404
	@GetMapping("/{productId}")
	public ResponseEntity<ProductDetailDTO> getProductDetail(@PathVariable Integer productId) {
		ProductDetailDTO detail = productService.getProductDetail(productId);
		return (detail != null) ? ResponseEntity.ok(detail) : ResponseEntity.notFound().build();
	}

	// 讀取圖片（Spring ResponseEntity 版：header / body / 狀態碼都宣告式交給 Spring 寫）
	@GetMapping("/{productId}/image")
	public ResponseEntity<byte[]> getProductImage(@PathVariable Integer productId) throws IOException {
		// 只撈圖片這一個欄位，不載入整個 ProductVO（零售價、描述…都不會被 SELECT）
		byte[] img = productService.getProductImageBytes(productId);

		// 沒商品 / 沒圖 → 回 404（用 notFound() 建造，不必自己 setStatus）
		if (img == null || img.length == 0) {
			return ResponseEntity.notFound().build();
		}

		// 判斷圖片類型（回字串，如 "image/png"）；判不出來預設 image/jpeg
		// contentType() 吃 MediaType 物件，所以要把字串 parse 成物件（不是 IMAGE_JPEG_VALUE 字串）
		String type = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(img));
		MediaType mediaType = (type != null) ? MediaType.parseMediaType(type) : MediaType.IMAGE_JPEG;

		return ResponseEntity.ok() // 200
				.contentType(mediaType) // 設 Content-Type，Spring 幫你寫進 header
				.body(img); // body，Spring 幫你寫進 response，不用自己 out.write
	}

	// ===== 下面是舊的 Servlet 版（對照保留，已停用；不可與上面同時啟用，GET 路徑會衝突）=====
	//
	// @GetMapping("/{productId}/image")//servlet寫法
	// public void getHandleImg(HttpServletResponse res, @PathVariable Integer
	// productId) throws IOException {
	// byte[] img = productService.getProductImageBytes(productId);
	// ServletOutputStream out = res.getOutputStream(); // ① 自己拿輸出串流
	// if (img != null && img.length > 0) {
	// // guessContentTypeFromStream：偷看開頭幾個 byte 鑑定型別，判不出就當 jpeg
	// String type = URLConnection.guessContentTypeFromStream(new
	// ByteArrayInputStream(img));
	// res.setContentType(type != null ? type : MediaType.IMAGE_JPEG_VALUE);// ② 自己設
	// header
	// out.write(img); // ③ 自己寫 body
	// } else {
	// res.setStatus(HttpStatus.NOT_FOUND.value()); // ④ 沒圖自己設 404
	// }
	// }

	@PostMapping("/{productId}/wishlist")
	public ResponseEntity<Void> addWishList(@PathVariable Integer productId,
			@AuthenticationPrincipal MemberUserDetails me) {

		boolean added = productService.addWishList(productId, me.getUserId());
		return added ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();

	}

	@DeleteMapping("/{productId}/wishlist")
	public ResponseEntity<Void> deleteWishList(@PathVariable Integer productId,
			@AuthenticationPrincipal MemberUserDetails me) {

		boolean deleted = productService.deleteWishList(productId, me.getUserId());
		return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();

	}

	@GetMapping("/wishlist")
	public ResponseEntity<List<ProductSummeryDTO>> getWishList(@AuthenticationPrincipal MemberUserDetails me) {

		List<ProductSummeryDTO> wishlists = productService.getAllWishLists(me.getUserId());
		return ResponseEntity.ok(wishlists);
	}

}
