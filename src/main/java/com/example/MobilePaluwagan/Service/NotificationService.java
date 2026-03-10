package com.example.MobilePaluwagan.Service;

import com.example.MobilePaluwagan.DTOs.Response.NotificationResponse;
import com.example.MobilePaluwagan.Entity.Notification;
import com.example.MobilePaluwagan.Entity.NotificationType;
import com.example.MobilePaluwagan.Repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    private NotificationResponse toDTO(Notification notif) {
        return new NotificationResponse(
                notif.getId(),
                notif.getTitle(),
                notif.getMessage(),
                notif.getType().name(),
                notif.getIsRead(),
                notif.getAccountNumber(),
                notif.getCreatedAt()
        );
    }


    public void notifyLoanSubmitted(String applicationId, String userName) {
        Notification adminNotif = new Notification();
        adminNotif.setIsAdmin(true);
        adminNotif.setTitle("New Loan Application");
        adminNotif.setMessage(userName + " submitted a new loan application.");
        adminNotif.setType(NotificationType.LOAN);
        adminNotif.setReferenceId(applicationId);
        adminNotif.setIsRead(false);
        adminNotif.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(adminNotif);
    }

    public void notifySavingsWithdraw(String savingsId, String userName) {
        Notification adminNotif = new Notification();
        adminNotif.setIsAdmin(true);
        adminNotif.setTitle("New Withdrawal Application");
        adminNotif.setMessage(userName + " submitted a new withdrawal application.");
        adminNotif.setType(NotificationType.SAVINGS);
        adminNotif.setReferenceId(savingsId);
        adminNotif.setIsRead(false);
        adminNotif.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(adminNotif);
    }

    // When loan is approved — notify user
    public void notifyLoanApproved(Long userId, String applicationId) {
        Notification userNotif = new Notification();
        userNotif.setUserId(userId);
        userNotif.setIsAdmin(false);
        userNotif.setTitle("Loan Approved! 🎉");
        userNotif.setMessage("Your loan application has been approved.");
        userNotif.setType(NotificationType.LOAN);
        userNotif.setReferenceId(applicationId);
        userNotif.setIsRead(false);
        userNotif.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(userNotif);
    }

    // When loan is rejected — notify user
    public void notifyLoanRejected(Long userId, String applicationId) {
        Notification userNotif = new Notification();
        userNotif.setUserId(userId);
        userNotif.setIsAdmin(false);
        userNotif.setTitle("Loan Rejected");
        userNotif.setMessage("Your loan application has been rejected.");
        userNotif.setType(NotificationType.LOAN);
        userNotif.setReferenceId(applicationId);
        userNotif.setIsRead(false);
        userNotif.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(userNotif);
    }

    // When payment is made — notify both
    public void notifyUserPaymentMade(Long userId, String applicationId, String userName, BigDecimal amount) {

        // Notify user
        Notification userNotif = new Notification();
        userNotif.setUserId(userId);
        userNotif.setIsAdmin(false);
        userNotif.setTitle("Payment Received");
        userNotif.setMessage("Your payment of ₱" + amount + " has been recorded.");
        userNotif.setType(NotificationType.SAVINGS);
        userNotif.setReferenceId(applicationId);
        userNotif.setIsRead(false);
        userNotif.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(userNotif);

    }

    public void notifyAdminPaymentMade(String applicationId, String userName, Double amount, String referenceId) {

        // Notify admin
        Notification adminNotif = new Notification();
        adminNotif.setIsAdmin(true);
        adminNotif.setUserId(null);
        adminNotif.setTitle("Payment Received");
        adminNotif.setMessage(userName + " made a payment of ₱" + amount + ".");
        adminNotif.setType(NotificationType.SAVINGS);
        adminNotif.setReferenceId(applicationId);
        adminNotif.setIsRead(false);
        adminNotif.setAccountNumber(applicationId);
        adminNotif.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(adminNotif);
    }

    // --- Fetch Notifications ---

    public List<NotificationResponse> getUserNotifications(Long userId) {
        return notificationRepository
                .findByUserIdAndIsAdminFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationResponse> getAdminNotifications() {
        return notificationRepository
                .findByIsAdminTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // --- Unread Count (for bell icon badge) ---

    public Long getUserUnreadCount(Long userId) {
        return notificationRepository
                .countByUserIdAndIsReadFalseAndIsAdminFalse(userId);
    }

    public Long getAdminUnreadCount() {
        return notificationRepository
                .countByIsAdminTrue();
    }

    // --- Mark as Read ---

    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    public void markSelectedAsRead(List<Long> notificationIds) {
        List<Notification> notifs = notificationRepository.findAllById(notificationIds);
        notifs.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(notifs);
    }

    public void markAllAdminAsRead(List<Long>  notificationIds) {
        List<Notification> notifs = notificationRepository.findAllById(notificationIds);
        notifs.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(notifs);
    }
}
