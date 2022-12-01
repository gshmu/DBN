package com.dci.intellij.dbn.common.dispose;

import com.intellij.openapi.progress.ProcessCanceledException;

public class AlreadyDisposedException extends ProcessCanceledException {
    @Deprecated // TODO only use for disposed object checks / use constructor on runtime
    public static final AlreadyDisposedException INSTANCE = new AlreadyDisposedException();
    private AlreadyDisposedException() {};
}
