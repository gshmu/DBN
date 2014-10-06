package com.dci.intellij.dbn.execution.statement.processor;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionResult;
import com.dci.intellij.dbn.execution.statement.variables.StatementExecutionVariablesBundle;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.object.DBSchema;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;

public interface StatementExecutionProcessor extends ConnectionProvider, Disposable{

    boolean isOrphan();

    boolean canExecute();

    ConnectionHandler getConnectionHandler();

    DBSchema getCurrentSchema();

    Project getProject();

    DBLanguagePsiFile getBoundPsiFile();

    String getResultName();

    String getStatementName();

    void navigateToResult();

    void navigateToEditor(boolean requestFocus);

    boolean promptVariablesDialog();

    void execute(ProgressIndicator progressIndicator);

    StatementExecutionVariablesBundle getExecutionVariables();

    void bind(ExecutablePsiElement executablePsiElement, boolean isExactMatch);

    void unbind();

    boolean isBound();

    ExecutablePsiElement getBoundExecutablePsiElement();

    StatementExecutionInput getExecutionInput();

    StatementExecutionResult getExecutionResult();
}
