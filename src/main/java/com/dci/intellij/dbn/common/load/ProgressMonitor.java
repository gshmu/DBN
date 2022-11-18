package com.dci.intellij.dbn.common.load;

import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import org.jetbrains.annotations.Nullable;

public final class ProgressMonitor {
    private ProgressMonitor() {}

    @Nullable
    public static ProgressIndicator getProgressIndicator() {
        return ProgressManager.getInstance().getProgressIndicator();
    }

    private static ProgressIndicator progress() {
        ProgressIndicator progressIndicator = getProgressIndicator();
        return progressIndicator == null ? DevNullProgressIndicator.INSTANCE : progressIndicator;
    }

    public static void checkCancelled() {
        //ProgressManager.checkCanceled();
        if (isCancelled()) {
            throw AlreadyDisposedException.INSTANCE;
        }
    }

    public static boolean isCancelled() {
        return progress().isCanceled();
    }

    public static void setProgressIndeterminate(boolean indeterminate) {
        progress().setIndeterminate(indeterminate);
    }

    public static void setProgressFraction(double fraction) {
        progress().setFraction(fraction);
    }

    public static void setProgressText(String text) {
        progress().setText(text);
    }

    public static void setProgressDetail(String subtext) {
        progress().setText2(subtext);
    }

}
