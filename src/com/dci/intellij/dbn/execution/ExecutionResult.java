package com.dci.intellij.dbn.execution;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.common.result.ui.ExecutionResultForm;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

public interface ExecutionResult extends Disposable {

    ExecutionResultForm getForm();

    @NotNull
    String getName();

    Icon getIcon();

    Project getProject();

    ConnectionHandler getConnectionHandler();

    PsiFile createPreviewFile();
}
