package com.dci.intellij.dbn.common.util;

import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.message.Message;
import com.dci.intellij.dbn.common.message.MessageBundle;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.intellij.openapi.ui.Messages;

public class MessageUtil {
    public static void showErrorDialog(MessageBundle messages, String title) {
        StringBuilder buffer = new StringBuilder();
        for (Message message : messages.getErrorMessages()) {
            buffer.append(message.getText());
            buffer.append("\n");
        }
        showErrorDialog(buffer.toString(), title);
    }

    public static void showErrorDialog(String message, Exception exception) {
        showErrorDialog(message, exception, null);
    }

    public static void showErrorDialog(String message, String title) {
        showErrorDialog(message, null, title);
    }

    public static void showErrorDialog(String message) {
        showErrorDialog(message, null, null);
    }

    public static void showErrorDialog(final String message, @Nullable final Exception exception, @Nullable final String title) {
        new ConditionalLaterInvocator() {
            public void execute() {
                String localMessage = message;
                String localTitle = title;
                if (exception != null) {
                    //String className = NamingUtil.getClassName(exception.getClass());
                    //message = message + "\nCause: [" + className + "] " + exception.getMessage();
                    localMessage = localMessage + "\n" + exception.getMessage();
                }
                if (localTitle == null) localTitle = "Error";
                Messages.showErrorDialog(localMessage, Constants.DBN_TITLE_PREFIX + "" + localTitle);
            }
        }.start();
    }

    public static void showInfoMessage(final String message, final String title) {
        new ConditionalLaterInvocator() {
            @Override
            public void execute() {
                Messages.showInfoMessage(message, Constants.DBN_TITLE_PREFIX + title);
            }
        }.start();
    }


}
