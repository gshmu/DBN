package com.dci.intellij.dbn.common.load;

import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;

public class ProgressMonitor {
    public static ProgressIndicator getProgressIndicator() {
        return ProgressManager.getInstance().getProgressIndicator();
    }

    public static String getTaskDescription() {
        ProgressIndicator progressIndicator = getProgressIndicator();
        if (progressIndicator != null) {
            return progressIndicator.getText();
        }
        return null;
    }

    public static void setTaskDescription(String description) {
        ProgressIndicator progressIndicator = getProgressIndicator();
        if (progressIndicator != null) {
            progressIndicator.setText(description);
        }
    }

    public static void setSubtaskDescription(String subtaskDescription) {
        ProgressIndicator progressIndicator = getProgressIndicator();
        if (progressIndicator != null) {
            progressIndicator.setText2(subtaskDescription);
        }
    }

    public static boolean isCancelled() {
        ProgressIndicator progressIndicator = getProgressIndicator();
        return progressIndicator != null && progressIndicator.isCanceled();
    }

    public static void checkCancelled() {
        if (isCancelled()) {
            throw AlreadyDisposedException.INSTANCE;
        }
    }
}
