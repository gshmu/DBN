package com.dci.intellij.dbn.common.notification;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

public interface NotificationSupport {
    default void sendNotification(NotificationType type, String title, String message, Object ... args) {
        NotificationUtil.sendNotification(getProject(), type, title, message, args);
    }

    default public void sendInfoNotification(String title, String message, Object ... args) {
        NotificationUtil.sendInfoNotification(getProject(), title, message, args);
    }

    default public void sendWarningNotification(String title, String message, Object ... args) {
        NotificationUtil.sendWarningNotification(getProject(), title, message, args);
    }

    default public void sendErrorNotification(String title, String message, Object ... args) {
        NotificationUtil.sendErrorNotification(getProject(), title, message, args);
    }

    Project getProject();
}
