package com.notificationapp.notificationgenerator.service;

import com.notificationapp.notificationgenerator.dto.EmailNotificationMessage;
import com.notificationapp.notificationgenerator.service.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationServiceImpl
        implements EmailNotificationService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.mail.subject-prefix}")
    private String subjectPrefix;

    @Override
    public void sendEmail(
            EmailNotificationMessage notificationMessage) {

        SimpleMailMessage mail = new SimpleMailMessage();

        mail.setFrom(fromAddress);
        mail.setTo(notificationMessage.getRecipientEmail());
        mail.setSubject(subjectPrefix + " Notification");
        mail.setText(notificationMessage.getMessage() + " " + notificationMessage.getTimestamp());

        mailSender.send(mail);

        log.info(
                "Email sent to {}",
                notificationMessage.getRecipientEmail()
        );
    }
}