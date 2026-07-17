package com.farmily.product.controller;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.farmily.product.dto.ProductInsertDTO;
import com.farmily.product.dto.ProductManageDTO;
import com.farmily.product.dto.ProductUpdatedDTO;
import com.farmily.product.service.ProductService;
import com.farmily.user.security.FarmerUserDetails;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/farmer/products")
public class FarmerProductController {

	@Autowired
	private ProductService productService;
	//用小農id去得到對應的商品頁面
	@GetMapping("/list")
	public ResponseEntity<List<ProductManageDTO>> getMyProducts(@AuthenticationPrincipal FarmerUserDetails me) {

		List<ProductManageDTO> products = productService.getMyProducts(me.getFarmerId());
		return ResponseEntity.ok(products);
	}

	// 新增商品
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Integer> addProduct(@Valid @ModelAttribute ProductInsertDTO dto,
			@AuthenticationPrincipal FarmerUserDetails me) throws IOException {

		Integer productId = productService.addProduct(dto, me.getFarmerId());

		return ResponseEntity.created(URI.create("/api/farmer/products/" + productId)).body(productId);
	}

	// 修改商品價格（只開放零售價 / 團購價/ 狀態，其他欄位一律改不到）
	// PATCH = 局部更新；收 JSON body，不再走 multipart（改價不需要圖片）
	@PatchMapping("/{productId}")
	public ResponseEntity<Void> updateProduct(@PathVariable Integer productId,
			@AuthenticationPrincipal FarmerUserDetails me, @Valid @RequestBody ProductUpdatedDTO dto) {
		boolean updated = productService.updateProduct(productId, dto, me.getFarmerId());
		return updated ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
	}
	//寫入資料庫時撞到關聯限制
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<String> handleDataIntegrityViolation(DataIntegrityViolationException e){
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("資料關聯有誤，請確認輸入的分類或商品是否存在");
	}

	// service 的業務檢查不過（查無此分類、團購價高於原價…）；訊息不寫死，直接用丟例外時塞的那句
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
	}
	
	@ExceptionHandler(BindException.class)
	public ResponseEntity<String> handleVaildation(BindException e){
		 List<FieldError> errors = new ArrayList<>(e.getBindingResult().getFieldErrors());
		 List<String> order = List.of ("subCatClassId","productName","retailPrice","groupPrice","unitPricingMeasure");
		 errors.sort(Comparator.comparingInt(item -> order.indexOf(item.getField())));
		 List<String> messages = new ArrayList<>();
		 for (FieldError error : errors) {
		     messages.add(error.getDefaultMessage());
		 }
		 String joined = String.join("、", messages);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(joined);
	}
	
}
