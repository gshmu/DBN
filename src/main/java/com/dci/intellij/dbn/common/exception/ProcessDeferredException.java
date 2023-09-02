package com.dci.intellij.dbn.common.exception;

import com.dci.intellij.dbn.common.compatibility.Compatibility;
import com.intellij.openapi.progress.ProcessCanceledException;

public class ProcessDeferredException extends ProcessCanceledException {
    public ProcessDeferredException() {
    }

    @Compatibility
    public ProcessDeferredException(String message) {
        // TODO super(String) no available in earlier ide versions
        super(new Exception(message));
    }
}
