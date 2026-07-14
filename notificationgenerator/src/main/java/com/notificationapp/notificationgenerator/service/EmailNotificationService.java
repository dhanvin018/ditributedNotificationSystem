package com.notificationapp.notificationgenerator.service;

import com.notificationapp.notificationgenerator.dto.EmailNotificationMessage;

public interface EmailNotificationService {

    void sendEmail(EmailNotificationMessage notificationMessage);
}