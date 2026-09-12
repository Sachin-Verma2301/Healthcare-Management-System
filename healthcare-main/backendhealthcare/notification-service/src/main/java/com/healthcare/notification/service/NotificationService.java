package com.healthcare.notification.service;

import com.healthcare.notification.model.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final Map<Long, Notification> notificationStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final JavaMailSender mailSender;

    @Autowired
    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        // No demo emails anymore — real notifications only, triggered by actual bookings
    }

    public Notification sendNotification(Notification notification) {
        if (notification.getId() == null) {
            notification.setId(idGenerator.getAndIncrement());
        }
        if (notification.getTimestamp() == null) {
            notification.setTimestamp(
                    LocalDateTime.now().format(
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    )
            );
        }
        if (notification.getNotificationType() == null) {
            notification.setNotificationType("EMAIL");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(notification.getRecipientEmail());
            message.setSubject(notification.getSubject());
            message.setText(notification.getMessage());
            mailSender.send(message);

            notification.setStatus("SENT");
            System.out.println(
                    "✅ [EMAIL SENT] To: " + notification.getRecipientEmail()
                            + " | Subject: " + notification.getSubject()
            );
        } catch (Exception e) {
            notification.setStatus("FAILED");
            System.out.println(
                    "❌ [EMAIL FAILED] To: " + notification.getRecipientEmail()
                            + " | Error: " + e.getMessage()
            );
        }

        notificationStore.put(notification.getId(), notification);
        return notification;
    }

    public List<Notification> getAllNotifications() {
        return new ArrayList<>(notificationStore.values());
    }

    public List<Notification> getNotificationsByRecipient(String email) {
        if (email == null) return Collections.emptyList();

        return notificationStore.values().stream()
                .filter(n -> email.equalsIgnoreCase(n.getRecipientEmail()))
                .collect(Collectors.toList());
    }

    public Map<String, Object> getNotificationSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalDispatched", notificationStore.size());
        summary.put("status", "ACTIVE");
        summary.put("servicePort", 8082);
        return summary;
    }
}