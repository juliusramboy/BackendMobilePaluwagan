package com.example.MobilePaluwagan.repository;

import com.example.MobilePaluwagan.dto.Response.MembersFilterProjection;
import com.example.MobilePaluwagan.dto.Response.MembersFilterResponse;
import com.example.MobilePaluwagan.entity.UserInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserInfoRepo extends JpaRepository<UserInfo, Long> {
    Optional<UserInfo> findByUserId(Long userId);

    @Query(value = "SELECT ul.id AS user_id, up.first_name, up.last_name, r.role_name, up.verified_date, ul.has_savings_account, ul.has_loan " +
            "FROM user_login ul " +
            "JOIN user_profile up ON ul.id = up.user_id " +
            "JOIN roles r ON r.id = ul.role_id " +
            "WHERE (CAST(:fullName AS text) IS NULL OR LOWER(CONCAT(up.first_name, ' ', up.last_name)) LIKE LOWER(CONCAT('%', CAST(:fullName AS text), '%'))) " +
            "AND (CAST(:role AS text) IS NULL OR r.role_name = CAST(:role AS text)) " +
            "ORDER BY up.first_name ASC",
            countQuery = "SELECT COUNT(*) FROM user_login ul " +
                    "JOIN user_profile up ON ul.id = up.user_id " +
                    "JOIN roles r ON r.id = ul.role_id " +
                    "WHERE (CAST(:fullName AS text) IS NULL OR LOWER(CONCAT(up.first_name, ' ', up.last_name)) LIKE LOWER(CONCAT('%', CAST(:fullName AS text), '%'))) " +
                    "AND (CAST(:role AS text) IS NULL OR r.role_name = CAST(:role AS text))",
            nativeQuery = true)
    Page<MembersFilterProjection> filterMembers(
            @Param("fullName") String fullName,
            @Param("role") String role,
            Pageable pageable
    );

}

