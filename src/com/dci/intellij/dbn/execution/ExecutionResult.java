package com.dci.intellij.dbn.execution;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.util.DataProviderSupplier;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.execution.common.result.ui.ExecutionResultForm;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

public interface ExecutionResult extends Disposable, DataProviderSupplier {

    @Nullable
    ExecutionResultForm getForm(boolean create);

    @NotNull
    String getName();

    Icon getIcon();

    @NotNull
    Project getProject();

    ConnectionId getConnectionId();

    @NotNull
    ConnectionHandler getConnectionHandler();

    PsiFile createPreviewFile();
}
