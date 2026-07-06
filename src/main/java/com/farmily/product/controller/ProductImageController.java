package com.farmily.product.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.farmily.product.service.ProductImageService;

@RestController
@RequestMapping("/api/products")
public class ProductImageController {
	@Autowired ProductImageService productImageService;
	//單一圖片查詢
	@GetMapping("/photo/image/{productImageId}")
	public ResponseEntity<byte[]> getProductImageById(@PathVariable Integer productImageId) throws IOException{
		byte[] img = productImageService.getProductImageById(productImageId);
		if(img==null || img.length==0) {
			return ResponseEntity.notFound().build();
		}
		String type = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(img));
		MediaType mediaType = (type!=null)?MediaType.parseMediaType(type) :MediaType.IMAGE_JPEG;
		return ResponseEntity.ok()
				.contentType(mediaType)
				.body(img);
	}
	//取商品有哪些圖片 id
	@GetMapping("/photo/{productId}")
    public ResponseEntity<List<Integer>> getImageIds(@PathVariable Integer productId) {
        List<Integer> ids = productImageService.getImageIdsByProductId(productId);
        return ids.isEmpty() ? ResponseEntity.notFound().build()
                             : ResponseEntity.ok(ids);
    }
}
