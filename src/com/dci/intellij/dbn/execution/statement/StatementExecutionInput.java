package com.dci.intellij.dbn.execution.statement;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.thread.ReadActionRunner;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.LazyValue;
import com.dci.intellij.dbn.common.util.SimpleLazyValue;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.execution.ExecutionContext;
import com.dci.intellij.dbn.execution.ExecutionInput;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.statement.options.StatementExecutionSettings;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.execution.statement.variables.StatementExecutionVariablesBundle;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.ExecutableBundlePsiElement;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

public class StatementExecutionInput implements ExecutionInput {
    private StatementExecutionProcessor executionProcessor;
    private StatementExecutionVariablesBundle executionVariables;
    private ConnectionHandlerRef connectionHandlerRef;
    private DBObjectRef<DBSchema> currentSchemaRef;

    private String originalStatementText;
    private String executableStatementText;
    private ExecutablePsiElement executablePsiElement;
    private boolean isBulkExecution = false;
    private boolean isDisposed;
    private int executionTimeout = 30;
    private int debugExecutionTimeout = 600;

    private LazyValue<ExecutionContext> executionContext = new SimpleLazyValue<ExecutionContext>() {
        @Override
        protected ExecutionContext load() {
            return new ExecutionContext() {
                @NotNull
                @Override
                public String getTargetName() {
                    ExecutablePsiElement executablePsiElement = getExecutablePsiElement();
                    return CommonUtil.nvl(executablePsiElement == null ? null : executablePsiElement.getPresentableText(), "Statement");
                }

                @Nullable
                @Override
                public ConnectionHandler getTargetConnection() {
                    return getConnectionHandler();
                }

                @Nullable
                @Override
                public DBSchema getTargetSchema() {
                    return getCurrentSchema();
                }
            };
        }
    };

    public StatementExecutionInput(String originalStatementText, String executableStatementText, StatementExecutionProcessor executionProcessor) {
        this.executionProcessor = executionProcessor;
        this.connectionHandlerRef = ConnectionHandlerRef.from(executionProcessor.getConnectionHandler());
        this.currentSchemaRef = DBObjectRef.from(executionProcessor.getCurrentSchema());
        this.originalStatementText = originalStatementText;
        this.executableStatementText = executableStatementText;

        StatementExecutionSettings statementExecutionSettings = ExecutionEngineSettings.getInstance(getProject()).getStatementExecutionSettings();
        executionTimeout = statementExecutionSettings.getExecutionTimeout();
        debugExecutionTimeout = statementExecutionSettings.getDebugExecutionTimeout();
    }

    public int getExecutableLineNumber() {
        return executionProcessor == null ? 0 : executionProcessor.getExecutableLineNumber();
    }

    public int getExecutionTimeout() {
        return executionTimeout;
    }

    public void setExecutionTimeout(int executionTimeout) {
        this.executionTimeout = executionTimeout;
    }

    @Override
    public int getDebugExecutionTimeout() {
        return debugExecutionTimeout;
    }

    public void setDebugExecutionTimeout(int debugExecutionTimeout) {
        this.debugExecutionTimeout = debugExecutionTimeout;
    }

    @NotNull
    @Override
    public ExecutionContext getExecutionContext() {
        return executionContext.get();
    }

    public void initExecution() {
        getExecutionContext().setExecutionTimestamp(System.currentTimeMillis());
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
            final DBSchema currentSchema = getCurrentSchema();
            if (connectionHandler != null) {
                executablePsiElement = new ReadActionRunner<ExecutablePsiElement>() {
                    @Override
                    protected ExecutablePsiElement run() {
                        DBLanguageDialect languageDialect = executionProcessor.getPsiFile().getLanguageDialect();
                        DBLanguagePsiFile previewFile = DBLanguagePsiFile.createFromText(getProject(), "preview", languageDialect, originalStatementText, connectionHandler, currentSchema);

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

    public PsiFile createPreviewFile() {
        ConnectionHandler activeConnection = getConnectionHandler();
        DBSchema currentSchema = getCurrentSchema();
        DBLanguageDialect languageDialect = activeConnection == null ?
                SQLLanguage.INSTANCE.getMainLanguageDialect() :
                activeConnection.getLanguageDialect(SQLLanguage.INSTANCE);

        return DBLanguagePsiFile.createFromText(getProject(), "preview", languageDialect, executableStatementText, activeConnection, currentSchema);
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

    public String getConnectionId() {
        return connectionHandlerRef == null ? null : connectionHandlerRef.getConnectionId();
    }

    @Nullable
    public DBSchema getCurrentSchema() {
        return DBObjectRef.get(currentSchemaRef);
    }

    public void setCurrentSchema(DBSchema currentSchema) {
        this.currentSchemaRef = DBObjectRef.from(currentSchema);
    }

    public boolean isBulkExecution() {
        return isBulkExecution;
    }

    public void setBulkExecution(boolean isBulkExecution) {
        this.isBulkExecution = isBulkExecution;
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

    public boolean isDatabaseLogProducer() {
        return executablePsiElement != null && executablePsiElement.getElementType().is(ElementTypeAttribute.DATABASE_LOG_PRODUCER);
    }
}
