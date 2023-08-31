package com.dci.intellij.dbn.common.exception;

import com.intellij.openapi.progress.ProcessCanceledException;

public class ProcessDeferredException extends ProcessCanceledException {
    public ProcessDeferredException() {
    }

    public ProcessDeferredException(String message) {
        super(message);
    }
}
