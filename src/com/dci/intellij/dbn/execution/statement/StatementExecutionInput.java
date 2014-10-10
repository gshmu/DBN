package com.dci.intellij.dbn.execution.statement;

import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.thread.ReadActionRunner;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.execution.statement.variables.StatementExecutionVariablesBundle;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.psi.ExecutableBundlePsiElement;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StatementExecutionInput implements Disposable {
    private StatementExecutionProcessor executionProcessor;
    private StatementExecutionVariablesBundle executionVariables;
    private ConnectionHandlerRef connectionHandlerRef;
    private DBObjectRef<DBSchema> currentSchemaRef;

    private String originalStatementText;
    private String executableStatementText;
    private ExecutablePsiElement executablePsiElement;
    private boolean isDisposed;

    public StatementExecutionInput(String originalStatementText, String executableStatementText, StatementExecutionProcessor executionProcessor) {
        this.executionProcessor = executionProcessor;
        this.connectionHandlerRef = ConnectionHandlerRef.from(executionProcessor.getConnectionHandler());
        this.currentSchemaRef = DBObjectRef.from(executionProcessor.getCurrentSchema());
        this.originalStatementText = originalStatementText;
        this.executableStatementText = executableStatementText;
    }

    public String getOriginalStatementText() {
        return originalStatementText;
    }

    public void setOriginalStatementText(String originalStatementText) {
        this.originalStatementText = originalStatementText;
        executablePsiElement = null;
    }

    public void setExecutableStatementText(String executableStatementText) {
        this.executableStatementText = executableStatementText;
    }

    public String getExecutableStatementText() {
        return executableStatementText;
    }

    @Nullable
    public ExecutablePsiElement getExecutablePsiElement() {
        if (executablePsiElement == null) {
            final ConnectionHandler connectionHandler = getConnectionHandler();
            if (connectionHandler != null) {
                executablePsiElement = new ReadActionRunner<ExecutablePsiElement>() {

                    @Override
                    protected ExecutablePsiElement run() {
                        PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(getProject());
                        DBLanguageDialect languageDialect = executionProcessor.getPsiFile().getLanguageDialect();
                        PsiFile previewFile = psiFileFactory.createFileFromText("preview", languageDialect, originalStatementText);

                        PsiElement firstChild = previewFile.getFirstChild();
                        if (firstChild instanceof ExecutableBundlePsiElement) {
                            ExecutableBundlePsiElement rootPsiElement = (ExecutableBundlePsiElement) firstChild;
                            List<ExecutablePsiElement> executablePsiElements = rootPsiElement.getExecutablePsiElements();
                            return executablePsiElements.isEmpty() ? null : executablePsiElements.get(0);
                        }
                        return null;
                    }
                }.start();
            }
        }
        return executablePsiElement;
    }

    public StatementExecutionVariablesBundle getExecutionVariables() {
        return executionVariables;
    }

    public void setExecutionVariables(StatementExecutionVariablesBundle executionVariables) {
        if (this.executionVariables != null) {
            DisposerUtil.dispose(this.executionVariables);
        }
        this.executionVariables = executionVariables;
    }

    public PsiFile getPreviewFile() {
        PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(getProject());
        ConnectionHandler connectionHandler = getConnectionHandler();
        DBLanguageDialect languageDialect = connectionHandler == null ?
                SQLLanguage.INSTANCE.getMainLanguageDialect() :
                connectionHandler.getLanguageDialect(SQLLanguage.INSTANCE);
        return psiFileFactory.createFileFromText("preview", languageDialect, executableStatementText);
    }

    public StatementExecutionProcessor getExecutionProcessor() {
        return executionProcessor;
    }

    public Project getProject() {
        return executionProcessor == null ? null : executionProcessor.getProject();
    }

    @Nullable
    public ConnectionHandler getConnectionHandler() {
        return ConnectionHandlerRef.get(connectionHandlerRef);
    }

    public void setConnectionHandler(ConnectionHandler connectionHandler) {
        this.connectionHandlerRef = ConnectionHandlerRef.from(connectionHandler);
    }

    @Nullable
    public DBSchema getCurrentSchema() {
        return DBObjectRef.get(currentSchemaRef);
    }

    public void setCurrentSchema(DBSchema currentSchema) {
        this.currentSchemaRef = DBObjectRef.from(currentSchema);
    }

    public void dispose() {
        if (!isDisposed) {
            isDisposed = true;
            executionProcessor = null;
            executablePsiElement = null;
        }
    }

    public boolean isDisposed() {
        return isDisposed;
    }

    public String getStatementDescription() {
        ExecutablePsiElement executablePsiElement = getExecutablePsiElement();
        return executablePsiElement == null ? "SQL Statement" : executablePsiElement.getPresentableText();
    }
}
