package com.dci.intellij.dbn.execution;

import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.execution.common.result.ui.ExecutionResultForm;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public interface ExecutionResult<F extends ExecutionResultForm> extends StatefulDisposable, DataProvider {

    @Nullable
    F createForm();

    @Nullable
    default F getForm() {
        return Dispatch.callConditional(() -> {
            Project project = getProject();
            ExecutionManager executionManager = ExecutionManager.getInstance(project);
            return (F) executionManager.getExecutionResultForm(this);
        });
    }

    @NotNull
    String getName();

    Icon getIcon();

    @NotNull
    Project getProject();

    ConnectionId getConnectionId();

    @NotNull
    ConnectionHandler getConnectionHandler();

    PsiFile createPreviewFile();

    ExecutionResult<F> getPrevious();

    void setPrevious(ExecutionResult<F> previous);
}
