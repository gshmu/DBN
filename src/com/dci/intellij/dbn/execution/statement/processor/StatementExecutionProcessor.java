package com.dci.intellij.dbn.execution.statement.processor;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionResult;
import com.dci.intellij.dbn.execution.statement.variables.StatementExecutionVariablesBundle;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.object.DBSchema;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;

public interface StatementExecutionProcessor extends ConnectionProvider, Disposable{
    void bind(ExecutablePsiElement executablePsiElement);

    boolean matches(ExecutablePsiElement executablePsiElement, boolean lenient);

    ExecutablePsiElement getExecutablePsiElement();

    // is orphan when isDirty and
    boolean isOrphan();

    // is dirty when the cached executablePsiElement is not valid any more as a result of file change
    boolean isDirty();

    boolean canExecute();

    ConnectionHandler getConnectionHandler();

    DBSchema getCurrentSchema();

    Project getProject();

    DBLanguagePsiFile getFile();

    String getResultName();

    String getStatementName();

    StatementExecutionResult getExecutionResult();

    void navigateToResult();

    void navigateToEditor(boolean requestFocus);

    boolean promptVariablesDialog();

    void execute(ProgressIndicator progressIndicator);

    StatementExecutionVariablesBundle getExecutionVariables();
}
