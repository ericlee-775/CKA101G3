package com.farmily.product.config;

import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ManyProductImage implements CommandLineRunner{
	@Autowired
	private JdbcTemplate jdbc;
	@Override
	public void run(String... args) throws Exception {
		// TODO Auto-generated method stub
		List<Integer> id = jdbc.queryForList("select product_image_id from product_image ORDER BY product_image_id",Integer.class);
		int count =1;
		 for (int Id : id) {
			 try {
				 ClassPathResource res = new ClassPathResource("sql/product/manyproductimages/" + count+ ".jpg");
				 byte[] bytes;
				 try (InputStream in = res.getInputStream()) {
				     bytes = in.readAllBytes();
				 }
	                jdbc.update(
	                        "UPDATE product_detail SET product_image = ? WHERE product_image_id = ? AND (product_image IS NULL OR LENGTH(product_image) = 0)",
	                        bytes, Id);
	                System.out.println("RayrayManyProduct JPG圖片第"+count+"筆載入成功");
	            } catch (Exception e) {
	            	try{
	            		ClassPathResource res = new ClassPathResource("sql/product/manyproductimages/" + count+ ".png");
	   				 byte[] bytes;
	   				 try (InputStream in = res.getInputStream()) {
	   				     bytes = in.readAllBytes();
	   				 }
	   	                jdbc.update(
	   	                        "UPDATE product_image SET product_image = ? WHERE product_image_id = ? AND (product_image IS NULL OR LENGTH(product_image) = 0)",
	   	                        bytes, Id);
	   	                System.out.println("RayrayManyProduct PNG圖片第"+count+"筆載入成功");
	            	}catch (Exception e2) {
	            		System.out.println("RayrayManyProduct圖片第"+count+"筆載入失敗"+e2.getMessage());
					}
	            	
	            }
			 count++;
	        }
	}
}
