package com.dci.intellij.dbn.execution.statement;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
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

public class StatementExecutionInput implements Disposable {
    private StatementExecutionProcessor executionProcessor;
    private ConnectionHandler connectionHandler;
    private DBObjectRef<DBSchema> schemaRef;

    private ExecutablePsiElement originalPsiElement;
    private String originalStatement;
    private String executeStatement;
    private boolean isDisposed;

    public StatementExecutionInput(String originalStatement, String executeStatement, StatementExecutionProcessor executionProcessor) {
        this.executionProcessor = executionProcessor;
        this.connectionHandler = executionProcessor.getActiveConnection();
        this.schemaRef = DBObjectRef.from(executionProcessor.getCurrentSchema());
        this.originalStatement = originalStatement;
        this.executeStatement = executeStatement;
    }


    public void setExecuteStatement(String executeStatement) {
        this.executeStatement = executeStatement;
    }

    public String getExecuteStatement() {
        return executeStatement;
    }

    public ExecutablePsiElement getExecutablePsiElement() {
        if (originalPsiElement == null) {
            PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(executionProcessor.getProject());
            PsiFile previewFile = psiFileFactory.createFileFromText("preview", connectionHandler.getLanguageDialect(SQLLanguage.INSTANCE), originalStatement);

            PsiElement firstChild = previewFile.getFirstChild();
            if (firstChild instanceof ExecutableBundlePsiElement) {
                ExecutableBundlePsiElement rootPsiElement = (ExecutableBundlePsiElement) firstChild;
                originalPsiElement = rootPsiElement.getExecutablePsiElements().get(0);
            }
        }
        return originalPsiElement;
    }

    public PsiFile getPreviewFile() {
        PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(executionProcessor.getProject());
        return psiFileFactory.createFileFromText("preview", connectionHandler.getLanguageDialect(SQLLanguage.INSTANCE), executeStatement);
    }

    public boolean isObsolete() {
        return  executionProcessor == null || executionProcessor.isOrphan() ||
                executionProcessor.getActiveConnection() != connectionHandler || // connection changed since execution
                executionProcessor.getCurrentSchema() != schemaRef || // current schema changed since execution
                (executionProcessor.getExecutablePsiElement() != null &&
                        executionProcessor.getExecutablePsiElement().matches(getExecutablePsiElement()) &&
                        !executionProcessor.getExecutablePsiElement().equals(getExecutablePsiElement()));
    }

    public StatementExecutionProcessor getExecutionProcessor() {
        return executionProcessor;
    }

    public Project getProject() {
        return executionProcessor.getProject();
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }

    public DBSchema getSchema() {
        return DBObjectRef.get(schemaRef);
    }

    public void dispose() {
        if (!isDisposed) {
            isDisposed = true;
            executionProcessor = null;
            connectionHandler = null;
            originalPsiElement = null;
        }
    }

    public boolean isDisposed() {
        return isDisposed;
    }
}
