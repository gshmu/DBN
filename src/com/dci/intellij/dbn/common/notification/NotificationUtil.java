package com.dci.intellij.dbn.common.notification;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;

import java.text.MessageFormat;

public class NotificationUtil {

    public static void sendInfoNotification(Project project, String title, String message, Object ... args) {
        sendNotification(project, NotificationType.INFORMATION, title, message, args);
    }

    public static void sendWarningNotification(Project project, String title, String message, Object ... args) {
        sendNotification(project, NotificationType.WARNING, title, message, args);
    }

    public static void sendErrorNotification(Project project, String title, String message, Object ... args) {
        sendNotification(project, NotificationType.ERROR, title, message, args);
    }

    public static void sendNotification(Project project, NotificationType type, String title, String message, Object ... args) {
        if (project != null && !project.isDisposed()) {
            NotificationListener listener = new NotificationListener.UrlOpeningListener(true);

            message = MessageFormat.format(message, args);
            Notification notification = new Notification("Database Navigator", title, message, type, listener);
            notification.setImportant(false);
            Notifications.Bus.notify(notification, project);
        }
    }
}
