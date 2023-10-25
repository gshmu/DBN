package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.common.compatibility.Compatibility;
import com.intellij.openapi.application.ApplicationManager;

/**
 * Internal API utilities - find alternatives
 */
@Compatibility
public class InternalApi {

    public static boolean isAppDisposeInProgress() {
        return ApplicationManager.getApplication().isDisposeInProgress();
    }
}
