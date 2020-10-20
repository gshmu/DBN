package com.dci.intellij.dbn.execution.statement.processor;

import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.navigation.NavigationInstructions;
import com.dci.intellij.dbn.common.ui.Presentable;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.execution.ExecutionContext;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionResult;
import com.dci.intellij.dbn.execution.statement.variables.StatementExecutionVariablesBundle;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

public interface StatementExecutionProcessor extends ConnectionProvider, StatefulDisposable, Presentable {

    boolean isDirty();

    @Override
    @Nullable
    ConnectionHandler getConnectionHandler();

    @NotNull
    ConnectionHandler getTargetConnection();

    @Nullable
    SchemaId getTargetSchema();

    @Nullable
    DatabaseSession getTargetSession();

    @NotNull
    Project getProject();

    @Nullable
    DBLanguagePsiFile getPsiFile();

    @Nullable
    VirtualFile getVirtualFile();

    @NotNull
    String getResultName();

    String getStatementName();

    void navigateToResult();

    void navigateToEditor(NavigationInstructions instructions);

    void execute() throws SQLException;

    void execute(@Nullable DBNConnection connection, boolean debug) throws SQLException;

    void postExecute();

    void cancelExecution();

    @Nullable
    StatementExecutionVariablesBundle getExecutionVariables();

    void bind(ExecutablePsiElement executablePsiElement);

    void unbind();

    boolean isBound();

    @Nullable
    FileEditor getFileEditor();

    @Nullable
    EditorProviderId getEditorProviderId();

    @Nullable
    ExecutablePsiElement getCachedExecutable();

    StatementExecutionInput getExecutionInput();

    @Nullable
    StatementExecutionResult getExecutionResult();

    void initExecutionInput(boolean bulkExecution);

    boolean isQuery();

    int getExecutableLineNumber();

    ExecutionContext getExecutionContext();

    ExecutionContext initExecutionContext();
}
