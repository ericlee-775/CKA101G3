package com.farmily.product.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.farmily.product.dto.ProductDTO;
import com.farmily.product.model.ProductVO;
import com.farmily.product.service.ProductService;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;
    
    // 查詢所有商品（統一回傳 DTO，對應前端 Vue 串接）
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products); 
    }

    // 新增商品
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductVO> addProduct(@ModelAttribute @Valid ProductVO productVO) {
        Integer productId = productService.addProduct(productVO);
        ProductVO newProduct = productService.getProductById(productId);
        return ResponseEntity.status(HttpStatus.CREATED).body(newProduct);
    }

    // 修改商品
    @PutMapping(value = "/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductVO> updateProduct(
            @PathVariable Integer productId,
            @ModelAttribute @Valid ProductVO productVO) {
            
        ProductVO product = productService.getProductById(productId);

        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        // 前端沒選新檔 → productImg 會是 null → 沿用 DB 裡的舊圖
        if (productVO.getProductImage() == null) {
            productVO.setProductImage(product.getProductImage());
        }

        productService.updateProduct(productId, productVO);
        ProductVO updateProduct = productService.getProductById(productId);

        return ResponseEntity.status(HttpStatus.OK).body(updateProduct);
    }

    // 讀取圖片
    @GetMapping("/{productId}/image")
    public void getHandleImg(HttpServletResponse res, @PathVariable Integer productId) throws IOException {
        ProductVO product = productService.getProductById(productId);
        ServletOutputStream out = res.getOutputStream();

        if (product != null && product.getProductImage() != null && product.getProductImage().length > 0) {
            byte[] img = product.getProductImage();
            // 自動判斷是 jpg/png/gif，判不出就當 jpeg
            String type = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(img));
            res.setContentType(type != null ? type : MediaType.IMAGE_JPEG_VALUE);
            out.write(img);
        } else {
            res.setStatus(HttpStatus.NOT_FOUND.value()); // 沒圖回傳 404
        }
    }
}
