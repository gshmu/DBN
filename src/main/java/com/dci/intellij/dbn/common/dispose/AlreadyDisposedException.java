package com.dci.intellij.dbn.common.dispose;

import com.intellij.openapi.progress.ProcessCanceledException;

public class AlreadyDisposedException extends ProcessCanceledException {
    public static final AlreadyDisposedException INSTANCE = new AlreadyDisposedException();
    private AlreadyDisposedException() {};
}
