package com.healthcare.notification.service;

import com.healthcare.notification.model.Notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {

    private NotificationService notificationService;


    @BeforeEach
    void setUp() {
        notificationService = new NotificationService();
    }


    // =========================================================
    // sendNotification() TEST CASES
    // =========================================================


    // TC-NS-01: Send notification successfully
    @Test
    void shouldSendNotificationSuccessfully() {

        Notification notification = new Notification(
                10L,
                "test@example.com",
                "Test User",
                "Test Subject",
                "Test Message",
                "EMAIL"
        );

        Notification result =
                notificationService.sendNotification(notification);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(
                "test@example.com",
                result.getRecipientEmail()
        );
        assertEquals(
                "Test Subject",
                result.getSubject()
        );
    }


    // TC-NS-02: Generate ID when notification ID is null
    @Test
    void shouldGenerateIdWhenNotificationIdIsNull() {

        Notification notification = new Notification();

        notification.setRecipientEmail("test@example.com");
        notification.setRecipientName("Test User");
        notification.setSubject("Test Subject");
        notification.setMessage("Test Message");

        Notification result =
                notificationService.sendNotification(notification);

        assertNotNull(result.getId());
    }


    // TC-NS-03: Set default status when status is null
    @Test
    void shouldSetDefaultStatus() {

        Notification notification = new Notification();

        notification.setId(20L);
        notification.setRecipientEmail("test@example.com");
        notification.setStatus(null);

        Notification result =
                notificationService.sendNotification(notification);

        assertEquals(
                "SENT",
                result.getStatus()
        );
    }


    // TC-NS-04: Set timestamp when timestamp is null
    @Test
    void shouldSetTimestampWhenNull() {

        Notification notification = new Notification();

        notification.setId(21L);
        notification.setRecipientEmail("test@example.com");
        notification.setTimestamp(null);

        Notification result =
                notificationService.sendNotification(notification);

        assertNotNull(result.getTimestamp());

        assertFalse(
                result.getTimestamp().isBlank()
        );
    }


    // TC-NS-05: Set default notification type when null
    @Test
    void shouldSetDefaultNotificationType() {

        Notification notification = new Notification();

        notification.setId(22L);
        notification.setRecipientEmail("test@example.com");
        notification.setNotificationType(null);

        Notification result =
                notificationService.sendNotification(notification);

        assertEquals(
                "EMAIL",
                result.getNotificationType()
        );
    }


    // =========================================================
    // getAllNotifications() TEST CASE
    // =========================================================


    // TC-NS-06: Get all notifications
    @Test
    void shouldGetAllNotificationsSuccessfully() {

        List<Notification> notifications =
                notificationService.getAllNotifications();

        // Two sample notifications are created
        // in the constructor.
        assertTrue(notifications.size() >= 2);
    }


    // =========================================================
    // getNotificationsByRecipient() TEST CASES
    // =========================================================


    // TC-NS-07: Get notifications by recipient email
    @Test
    void shouldGetNotificationsByRecipient() {

        List<Notification> result =
                notificationService.getNotificationsByRecipient(
                        "patient@hospital.com"
                );

        assertFalse(result.isEmpty());

        assertEquals(
                "patient@hospital.com",
                result.get(0).getRecipientEmail()
        );
    }


    // TC-NS-08: Return empty list when recipient email is null
    @Test
    void shouldReturnEmptyListWhenEmailIsNull() {

        List<Notification> result =
                notificationService.getNotificationsByRecipient(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    // =========================================================
    // getNotificationSummary() TEST CASE
    // =========================================================


    // TC-NS-09: Get notification summary
    @Test
    void shouldGetNotificationSummarySuccessfully() {

        Map<String, Object> summary =
                notificationService.getNotificationSummary();

        assertNotNull(summary);

        assertEquals(
                "ACTIVE",
                summary.get("status")
        );

        assertEquals(
                8082,
                summary.get("servicePort")
        );

        assertTrue(
                summary.containsKey("totalDispatched")
        );

        assertEquals(
                2,
                summary.get("totalDispatched")
        );
    }
}