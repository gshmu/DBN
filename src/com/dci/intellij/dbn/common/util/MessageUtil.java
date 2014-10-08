package com.dci.intellij.dbn.common.util;

import javax.swing.Icon;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.message.Message;
import com.dci.intellij.dbn.common.message.MessageBundle;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
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

    public static int showErrorDialog(String message, @Nullable Exception exception, @Nullable String title) {
        if (exception != null) {
            //String className = NamingUtil.getClassName(exception.getClass());
            //message = message + "\nCause: [" + className + "] " + exception.getMessage();
            message = message + "\n" + exception.getMessage();
        }
        if (title == null) title = "Error";
        return showDialog(message, title, OPTIONS_OK, 0, Icons.DIALOG_ERROR);
    }

    public static int showErrorDialog(final String message, final String title, String[] options, int defaultOptionIndex) {
        return showDialog(message, title, options, defaultOptionIndex, Icons.DIALOG_ERROR);
    }

    public static int showQuestionDialog(final String message, final String title, String[] options, int defaultOptionIndex) {
        return showDialog(message, title, options, defaultOptionIndex, Icons.DIALOG_QUESTION);
    }


    public static void showWarningDialog(final String message, final String title) {
        showWarningDialog(message, title, OPTIONS_OK, 0);
    }

    public static int showWarningDialog(final String message, final String title, String[] options, int defaultOptionIndex) {
        return showDialog(message, title, options, defaultOptionIndex, Icons.DIALOG_WARNING);
    }

    public static void showInfoDialog(final String message, final String title) {
        showInfoDialog(message, title, OPTIONS_OK, 0);
    }

    public static int showInfoDialog(final String message, final String title, String[] options, int defaultOptionIndex) {
        return showDialog(message, title, options, defaultOptionIndex, Icons.DIALOG_INFORMATION);
    }

    private static int showDialog(final String message, final String title, final String[] options, final int defaultOptionIndex, final Icon icon) {
        final AtomicInteger exitCode = new AtomicInteger(0);
        new SimpleLaterInvocator() {
            @Override
            public void execute() {
                int selectedOption = Messages.showDialog(message, Constants.DBN_TITLE_PREFIX + title, options, defaultOptionIndex, icon);
                exitCode.set(selectedOption);
            }
        }.start();
        return exitCode.get();
    }


}
