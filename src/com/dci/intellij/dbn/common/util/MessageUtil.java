package com.dci.intellij.dbn.common.util;

import javax.swing.Icon;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.message.Message;
import com.dci.intellij.dbn.common.message.MessageBundle;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.thread.RunnableTask;
import com.intellij.openapi.ui.Messages;

public class MessageUtil {

    public static final String[] OPTIONS_OK = new String[]{"OK"};
    public static final String[] OPTIONS_YES_NO = new String[]{"Yes", "No"};

    public static void showErrorDialog(MessageBundle messages, String title) {
        StringBuilder buffer = new StringBuilder();
        for (Message message : messages.getErrorMessages()) {
            buffer.append(message.getText());
            buffer.append("\n");
        }
        showErrorDialog(title, buffer.toString());
    }

    public static void showErrorDialog(String message, Exception exception) {
        showErrorDialog(message, exception, null);
    }

    public static void showErrorDialog(String title, String message) {
        showErrorDialog(message, null, title);
    }

    public static void showErrorDialog(String message) {
        showErrorDialog(message, null, null);
    }

    public static void showErrorDialog(String message, @Nullable Exception exception, @Nullable String title) {
        if (exception != null) {
            //String className = NamingUtil.getClassName(exception.getClass());
            //message = message + "\nCause: [" + className + "] " + exception.getMessage();
            message = message + "\n" + exception.getMessage();
        }
        if (title == null) title = "Error";
        showDialog(message, title, OPTIONS_OK, 0, Icons.DIALOG_ERROR, null);
    }

    public static void showErrorDialog(final String message, final String title, String[] options, int defaultOptionIndex, RunnableTask callback) {
        showDialog(message, title, options, defaultOptionIndex, Icons.DIALOG_ERROR, callback);
    }

    public static void showQuestionDialog(final String title, final String message, String[] options, int defaultOptionIndex, RunnableTask callback) {
        showDialog(message, title, options, defaultOptionIndex, Icons.DIALOG_QUESTION, callback);
    }


    public static void showWarningDialog(final String message, final String title) {
        showWarningDialog(message, title, OPTIONS_OK, 0, null);
    }

    public static void showWarningDialog(final String message, final String title, String[] options, int defaultOptionIndex, RunnableTask callback) {
        showDialog(message, title, options, defaultOptionIndex, Icons.DIALOG_WARNING, callback);
    }

    public static void showInfoDialog(final String title, final String message) {
        showInfoDialog(title, message, OPTIONS_OK, 0, null);
    }

    public static void showInfoDialog(final String title, final String message, String[] options, int defaultOptionIndex, RunnableTask callback) {
        showDialog(message, title, options, defaultOptionIndex, Icons.DIALOG_INFORMATION, callback);
    }

    private static void showDialog(
            final String message,
            final String title,
            final String[] options,
            final int defaultOptionIndex,
            final Icon icon,
            final RunnableTask callback) {
        new ConditionalLaterInvocator() {
            @Override
            public void execute() {
                int option = Messages.showDialog(message, Constants.DBN_TITLE_PREFIX + title, options, defaultOptionIndex, icon);
                if (callback != null) {
                    callback.setOption(option);
                    callback.start();
                }

            }
        }.start();
    }


}
