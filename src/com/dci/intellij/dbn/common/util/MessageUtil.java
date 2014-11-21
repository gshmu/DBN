package com.dci.intellij.dbn.common.util;

import javax.swing.Icon;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.message.Message;
import com.dci.intellij.dbn.common.message.MessageBundle;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.thread.SimpleTask;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

public class MessageUtil {

    public static final String[] OPTIONS_OK = new String[]{"OK"};
    public static final String[] OPTIONS_YES_NO = new String[]{"Yes", "No"};

    public static void showErrorDialog(@Nullable Project project, String title, MessageBundle messages) {
        StringBuilder buffer = new StringBuilder();
        for (Message message : messages.getErrorMessages()) {
            buffer.append(message.getText());
            buffer.append("\n");
        }
        showErrorDialog(project, title, buffer.toString());
    }

    public static void showErrorDialog(@Nullable Project project, String message, Exception exception) {
        showErrorDialog(project, null, message, exception);
    }

    public static void showErrorDialog(@Nullable Project project, String title, String message) {
        showErrorDialog(project, title, message, null);
    }

    public static void showErrorDialog(@Nullable Project project, String message) {
        showErrorDialog(project, null, message, null);
    }

    public static void showErrorDialog(@Nullable Project project, @Nullable String title, String message, @Nullable Exception exception) {
        if (exception != null) {
            //String className = NamingUtil.getClassName(exception.getClass());
            //message = message + "\nCause: [" + className + "] " + exception.getMessage();
            message = message + "\n" + exception.getMessage();
        }
        if (title == null) title = "Error";
        showDialog(project, message, title, OPTIONS_OK, 0, Icons.DIALOG_ERROR, null);
    }

    public static void showErrorDialog(@Nullable Project project, String title, String message, String[] options, int defaultOptionIndex, SimpleTask callback) {
        showDialog(project, message, title, options, defaultOptionIndex, Icons.DIALOG_ERROR, callback);
    }

    public static void showQuestionDialog(@Nullable Project project, String title, String message, String[] options, int defaultOptionIndex, SimpleTask callback) {
        showDialog(project, message, title, options, defaultOptionIndex, Icons.DIALOG_QUESTION, callback);
    }


    public static void showWarningDialog(@Nullable Project project, String title, String message) {
        showWarningDialog(project, title, message, OPTIONS_OK, 0, null);
    }

    public static void showWarningDialog(@Nullable Project project, String title, String message, String[] options, int defaultOptionIndex, SimpleTask callback) {
        showDialog(project, message, title, options, defaultOptionIndex, Icons.DIALOG_WARNING, callback);
    }

    public static void showInfoDialog(@Nullable Project project, String title, String message) {
        showInfoDialog(project, title, message, OPTIONS_OK, 0, null);
    }

    public static void showInfoDialog(@Nullable Project project, String title, String message, String[] options, int defaultOptionIndex, SimpleTask callback) {
        showDialog(project, message, title, options, defaultOptionIndex, Icons.DIALOG_INFORMATION, callback);
    }

    private static void showDialog(
            @Nullable final Project project, final String message,
            final String title,
            final String[] options,
            final int defaultOptionIndex,
            final Icon icon,
            final SimpleTask callback) {
        new ConditionalLaterInvocator() {
            @Override
            public void execute() {
                int option = Messages.showDialog(project, message, Constants.DBN_TITLE_PREFIX + title, options, defaultOptionIndex, icon);
                if (callback != null) {
                    callback.setOption(option);
                    callback.start();
                }

            }
        }.start();
    }


}
