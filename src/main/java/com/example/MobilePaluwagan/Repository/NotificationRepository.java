package com.example.MobilePaluwagan.Repository;

import com.example.MobilePaluwagan.Entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdAndIsAdminFalseOrderByCreatedAtDesc(Long userId);
    List<Notification> findByIsAdminTrueOrderByCreatedAtDesc();

    Long countByUserIdAndIsAdminFalse(Long userId);
    Long countByIsAdminTrue();

    void deleteByUserIdAndIsAdminFalse(Long userId);   // delete all user notifs
    void deleteByIsAdminTrue();
}
