package com.example.MobilePaluwagan.Service;

import com.example.MobilePaluwagan.Entity.Notification;
import com.example.MobilePaluwagan.Entity.NotificationType;
import com.example.MobilePaluwagan.Repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;


    public void notifyLoanSubmitted(String applicationId, String userName) {
        Notification adminNotif = new Notification();
        adminNotif.setIsAdmin(true);
        adminNotif.setTitle("New Loan Application");
        adminNotif.setMessage(userName + " submitted a new loan application.");
        adminNotif.setType(NotificationType.LOAN_SUBMITTED);
        adminNotif.setReferenceId(applicationId);
        adminNotif.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(adminNotif);
    }

    public void notifySavingsWithdraw(String savingsId, String userName) {
        Notification adminNotif = new Notification();
        adminNotif.setIsAdmin(true);
        adminNotif.setTitle("New Withdrawal Application");
        adminNotif.setMessage(userName + " submitted a new withdrawal application.");
        adminNotif.setType(NotificationType.SAVINGS_WITHDRAWN);
        adminNotif.setReferenceId(savingsId);
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
        userNotif.setType(NotificationType.LOAN_APPROVED);
        userNotif.setReferenceId(applicationId);
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
        userNotif.setType(NotificationType.LOAN_REJECTED);
        userNotif.setReferenceId(applicationId);
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
        userNotif.setType(NotificationType.PAYMENT_MADE);
        userNotif.setReferenceId(applicationId);
        userNotif.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(userNotif);

    }

    public void notifyAdminPaymentMade(String applicationId, String userName, Double amount) {

        // Notify admin
        Notification adminNotif = new Notification();
        adminNotif.setIsAdmin(true);
        adminNotif.setUserId(null);
        adminNotif.setTitle("Payment Received");
        adminNotif.setMessage(userName + " made a payment of ₱" + amount + ".");
        adminNotif.setType(NotificationType.PAYMENT_MADE);
        adminNotif.setReferenceId(applicationId);
        adminNotif.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(adminNotif);
    }

    // --- Fetch Notifications ---

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository
                .findByUserIdAndIsAdminFalseOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getAdminNotifications() {
        return notificationRepository
                .findByIsAdminTrueOrderByCreatedAtDesc();
    }

    // --- Unread Count (for bell icon badge) ---

    public Long getUserUnreadCount(Long userId) {
        return notificationRepository
                .countByUserIdAndIsAdminFalse(userId);
    }

    public Long getAdminUnreadCount() {
        return notificationRepository
                .countByIsAdminTrue();
    }

    // --- Mark as Read ---

    public void markAsRead(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    public void markAllAsRead(Long userId) {
        notificationRepository.deleteByUserIdAndIsAdminFalse(userId);
    }

    public void markAllAdminAsRead(){
        notificationRepository.deleteByIsAdminTrue();
    }
}
