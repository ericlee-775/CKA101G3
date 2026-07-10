package com.farmily.product.model;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.farmily.product.dto.SubCategoryOptionDTO;

public interface SubCategoryRepository extends JpaRepository<SubCategoryVO, Integer> {
	@Cacheable("categories")
	@Query("SELECT new com.farmily.product.dto.SubCategoryOptionDTO(s.subCatClassId, s.subCatClassName, m.productMainCatId, m.productMainCatName)"
			+ " from SubCategoryVO s JOIN s.mainCategoryVO m")
	List<SubCategoryOptionDTO> findAllOptions();
	
}
