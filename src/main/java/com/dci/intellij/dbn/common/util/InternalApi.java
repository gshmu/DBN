package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.common.compatibility.Compatibility;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.objectTree.ThrowableInterner;
import org.jetbrains.annotations.NotNull;

/**
 * Internal API utilities - find alternatives
 */
@Compatibility
public class InternalApi {

    @NotNull
    static Throwable getThrowableIntern(Throwable trace) {
        return ThrowableInterner.intern(trace);
    }

    public static boolean isAppDisposeInProgress() {
        return ApplicationManager.getApplication().isDisposeInProgress();
    }
}
