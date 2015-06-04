package com.dci.intellij.dbn.execution.statement.processor;

import java.sql.Connection;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionResult;
import com.dci.intellij.dbn.execution.statement.variables.StatementExecutionVariablesBundle;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.object.DBSchema;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public interface StatementExecutionProcessor extends ConnectionProvider, Disposable{

    boolean isDirty();

    @Nullable
    ConnectionHandler getConnectionHandler();

    @Nullable
    DBSchema getCurrentSchema();

    @NotNull
    Project getProject();

    @NotNull
    DBLanguagePsiFile getPsiFile();

    VirtualFile getVirtualFile();

    @NotNull
    String getResultName();

    String getStatementName();

    void navigateToResult();

    @Deprecated
    void navigateToEditor(boolean requestFocus);

    void execute();

    void execute(@Nullable Connection connection);

    StatementExecutionVariablesBundle getExecutionVariables();

    void bind(ExecutablePsiElement executablePsiElement);

    void unbind();

    boolean isBound();

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

    List<StatementExecutionProcessor> asList();
}
