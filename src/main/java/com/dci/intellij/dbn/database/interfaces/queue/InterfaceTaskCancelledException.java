package com.dci.intellij.dbn.database.interfaces.queue;

import com.intellij.openapi.progress.ProcessCanceledException;

public class InterfaceTaskCancelledException extends ProcessCanceledException {
    public static final InterfaceTaskCancelledException INSTANCE = new InterfaceTaskCancelledException();
}
