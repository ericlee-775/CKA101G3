package com.farmily.product.model;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductVO, Integer>{

	// 新增 / 修改用 save()、查詢用 findById()，皆由 JpaRepository 提供，不需另外宣告

}
