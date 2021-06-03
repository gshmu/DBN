package com.dci.intellij.dbn.common.util;

import com.intellij.openapi.application.ex.ApplicationManagerEx;

public class InternalApiUtil {
    public static boolean isApplicationExitInProgress() {
        return ApplicationManagerEx.getApplicationEx().isExitInProgress();
    }

}
