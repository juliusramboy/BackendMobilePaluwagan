package com.example.MobilePaluwagan.Repository;

import com.example.MobilePaluwagan.Entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdAndIsAdminFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<Notification> findByIsAdminTrueOrderByCreatedAtDesc(Pageable pageable);

    Long countByUserIdAndIsReadFalseAndIsAdminFalse(Long userId);
    Long countByIsAdminTrue();

    void deleteByUserIdAndIsAdminFalse(Long userId);   // delete all user notifs
    void deleteByIsAdminTrue();
}
