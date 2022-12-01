package com.dci.intellij.dbn.code.common.completion;

import com.intellij.openapi.progress.ProcessCanceledException;

public class CodeCompletionCancelledException extends ProcessCanceledException {
    public static final CodeCompletionCancelledException INSTANCE = new CodeCompletionCancelledException();
}
