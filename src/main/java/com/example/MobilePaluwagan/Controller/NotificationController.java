package com.example.MobilePaluwagan.Controller;

import com.example.MobilePaluwagan.DTOs.Response.NotificationResponse;
import com.example.MobilePaluwagan.Service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {


    @Autowired
    private NotificationService notificationService;

    // user — get all notifications
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<NotificationResponse>> getUserNotifications(@PathVariable Long userId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId, page, size));
    }

    // user — get unread count (bell badge)
    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Long> getUserUnreadCount(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUserUnreadCount(userId));
    }


    // mark single notification as read
    @PatchMapping("/user/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    // mark all as read for designated for user
    @PatchMapping("/user/read-selected")
    public ResponseEntity<Void> markSelectedAsRead(@RequestBody List<Long> notificationIds) {
        notificationService.markSelectedAsRead(notificationIds);
        return ResponseEntity.ok().build();
    }



    // admin — get all notifications
    @GetMapping("/admin")
    public ResponseEntity<Page<NotificationResponse>> getAdminNotifications( @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok( notificationService.getAdminNotifications(page, size));
    }

    // admin — get unread count (bell badge)
    @GetMapping("/admin/unread-count")
    public ResponseEntity<Long> getAdminUnreadCount() {
        return ResponseEntity.ok(notificationService.getAdminUnreadCount());
    }

    @PatchMapping("/admin/{notificationId}/read")
    public ResponseEntity<Void> markAsReadAdmin(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    // mark all as read for designated for admin
    @PatchMapping("/admin/read-selected")
    public ResponseEntity<Void> markAllAsReadAdmin(@RequestBody List<Long> notificationIds) {
        notificationService.markAllAdminAsRead(notificationIds);
        return ResponseEntity.ok().build();
    }

    // delete — all notifications both user and admin
    @DeleteMapping("/clear-all")
    public ResponseEntity<Void> clearUserAllNotifications(@RequestBody List<Long> notificationIds) {
        notificationService.clearAllNotifications(notificationIds);
        return ResponseEntity.ok().build();
    }


}
