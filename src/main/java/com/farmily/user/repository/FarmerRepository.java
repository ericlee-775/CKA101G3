package com.farmily.user.repository;

import com.farmily.user.model.Farmer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FarmerRepository extends JpaRepository<Farmer, Integer> {

    Optional<Farmer> findByEmail(String email);
    boolean existsByEmail (String email);       // 檢查 email 全系統唯一

    // 管理員端小農複合查詢 + 分頁（原生 SQL）：
    // keyword 比對農場名 / email / 電話；status 比對小農狀態；皆可為 null（null 代表不套用該條件）
    @Query(value = """
            SELECT f.* FROM FARMER f
            WHERE (:keyword IS NULL
                   OR f.farm_name LIKE CONCAT('%', :keyword, '%')
                   OR f.email LIKE CONCAT('%', :keyword, '%')
                   OR f.farmer_phone_num LIKE CONCAT('%', :keyword, '%'))
              AND (:status IS NULL OR f.farmer_status = :status)
            """,
            countQuery = """
            SELECT COUNT(*) FROM FARMER f
            WHERE (:keyword IS NULL
                   OR f.farm_name LIKE CONCAT('%', :keyword, '%')
                   OR f.email LIKE CONCAT('%', :keyword, '%')
                   OR f.farmer_phone_num LIKE CONCAT('%', :keyword, '%'))
              AND (:status IS NULL OR f.farmer_status = :status)
            """,
            nativeQuery = true)
    Page<Farmer> searchFarmers(@Param("keyword") String keyword,
                               @Param("status") String status,
                               Pageable pageable);
    
    
    // 管理員小農端複合查詢 (系統公告)
    // 比對多種 statuses, 回傳 List Ids
    @Query("SELECT f.farmerId FROM Farmer f WHERE f.farmerStatus IN :statuses")
    Set<Integer> findIdsByStatusIn(@Param("statuses") List<Farmer.FarmerStatus> statuses);

}
