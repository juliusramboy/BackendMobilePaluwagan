package com.example.MobilePaluwagan.repository;

import com.example.MobilePaluwagan.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdAndIsAdminFalseOrderByIsReadAscCreatedAtDesc(Long userId, Pageable pageable);
    Page<Notification> findByIsAdminTrueOrderByCreatedAtDesc(Pageable pageable);

    Long countByUserIdAndIsReadFalseAndIsAdminFalse(Long userId);
    Long countByIsAdminTrueAndIsReadFalse();


}
