package com.dci.intellij.dbn.execution.method.result.action;

import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.intellij.openapi.project.DumbAwareAction;

import javax.swing.*;

public abstract class MethodExecutionResultAction extends DumbAwareAction {
    protected MethodExecutionResult executionResult;

    public MethodExecutionResultAction(MethodExecutionResult executionResult, String text, Icon icon) {
        super(text, null, icon);
        this.executionResult = executionResult;
    }
}
