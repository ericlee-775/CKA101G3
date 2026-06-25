package com.farmily.product.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity; // 建議使用 Spring 內建的 ResponseEntity
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.farmily.product.dto.ProductDTO;
import com.farmily.product.model.ProductVO;
import com.farmily.product.service.ProductService;

@RestController
@RequestMapping("/api/products") // 建議1：使用複數名詞，並設定統一前綴
public class ProductController {

    @Autowired
    private ProductService productService;
    
    @GetMapping // 路由會變成 GET /api/products
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = productService.getAllProducts();
        
        // 建議2：明確回傳 HTTP 200 OK 狀態碼與資料
        return ResponseEntity.ok(products); 
    }
}
