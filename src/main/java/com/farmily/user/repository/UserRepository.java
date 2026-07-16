package com.farmily.user.repository;

import com.farmily.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    // 檢查 email 全系統唯一
    boolean existsByEmail(String email);

    // OAuth: 用 Google 的編號 (providerId) 找會員
    Optional<User> findByProviderId(String providerId);
    //英文小教室 Verified===驗證
    List<User> findByUserStatusAndEmailVerifiedTrue(User.UserStatus userStatus);

    // 管理員端會員複合查詢 + 分頁（原生 SQL）：
    // keyword 比對 email / 姓名 / 電話；status 比對會員狀態；皆可為 null（null 代表不套用該條件）
    @Query(value = """
            SELECT u.* FROM `user` u
            WHERE (:keyword IS NULL
                   OR u.email LIKE CONCAT('%', :keyword, '%')
                   OR u.user_name LIKE CONCAT('%', :keyword, '%')
                   OR u.user_phone_num LIKE CONCAT('%', :keyword, '%'))
              AND (:status IS NULL OR u.user_status = :status)
            """,
            countQuery = """
            SELECT COUNT(*) FROM `user` u
            WHERE (:keyword IS NULL
                   OR u.email LIKE CONCAT('%', :keyword, '%')
                   OR u.user_name LIKE CONCAT('%', :keyword, '%')
                   OR u.user_phone_num LIKE CONCAT('%', :keyword, '%'))
              AND (:status IS NULL OR u.user_status = :status)
            """,
            nativeQuery = true)
    Page<User> searchMembers(@Param("keyword") String keyword,
                             @Param("status") String status,
                             Pageable pageable);




//    // -- 管理員端用:keyword 比對會員 email 或會員姓名; status 比對會員狀態,皆可為 null --
//    @Query(value = "SELECT u FROM User u WHERE " +
//            "(:keyword IS NULL OR u.email LIKE %:keyword% OR u.userName LIKE %:keyword%) AND " +
//            "(:status IS NULL OR u.userStatus = :status)", nativeQuery = false)
//
//    Page<User> search(@Param("keyword") String keyword,
//                      @Param("status") String status,
//                      Pageable pageable);

//    List<User> findByUserName(String userName);
//    List<User> findByUserStatus(User.UserStatus userStatus);
//    List<User> findByIsFarmerTrue();

    // 自定義查詢
//    User findByUserIdAndUserName(Integer userId, String userName);
//    Optional<User> findByEmailAndUserStatus(String email, User.UserStatus status);


    // 複雜查詢可以用 @Query 寫原生 SQL (nativeQuery = true)
//    @Query(value = "SELECT user_id, user_name FROM user WHERE user_id = ?1 AND user_name = ?2", nativeQuery = true)
//    User testUser(Integer userId, String userName);

}



