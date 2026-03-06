package com.example.MobilePaluwagan.Controller;

import com.example.MobilePaluwagan.Entity.Notification;
import com.example.MobilePaluwagan.Repository.NotificationRepository;
import com.example.MobilePaluwagan.Service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationService notificationService;

    // user — get all notifications
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    // admin — get all notifications
    @GetMapping("/admin")
    public ResponseEntity<List<Notification>> getAdminNotifications() {
        return ResponseEntity.ok(notificationService.getAdminNotifications());
    }

    // user — get unread count (bell badge)
    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Long> getUserUnreadCount(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUserUnreadCount(userId));
    }

    // admin — get unread count (bell badge)
    @GetMapping("/admin/unread-count")
    public ResponseEntity<Long> getAdminUnreadCount() {
        return ResponseEntity.ok(notificationService.getAdminUnreadCount());
    }

    // mark single notification as read
    @DeleteMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    // mark all as read for a user
    @DeleteMapping("/user/{userId}/read-all")
    public ResponseEntity<Void> markAllAsReadUser(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }
    // mark all as read for admin
    @DeleteMapping("/admin/{userId}/read-all")
    public ResponseEntity<Void> markAllAsReadAdmin() {
        notificationService.markAllAdminAsRead();
        return ResponseEntity.ok().build();
    }
}
