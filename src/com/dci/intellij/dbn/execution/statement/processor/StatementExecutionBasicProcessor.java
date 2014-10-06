package com.dci.intellij.dbn.execution.statement.processor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.common.thread.ReadActionRunner;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.compiler.CompilerAction;
import com.dci.intellij.dbn.execution.compiler.CompilerResult;
import com.dci.intellij.dbn.execution.statement.DataDefinitionChangeListener;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.dci.intellij.dbn.execution.statement.options.StatementExecutionSettings;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionBasicResult;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionResult;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionStatus;
import com.dci.intellij.dbn.execution.statement.variables.StatementExecutionVariablesBundle;
import com.dci.intellij.dbn.execution.statement.variables.ui.StatementExecutionVariablesDialog;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.ChameleonPsiElement;
import com.dci.intellij.dbn.language.common.psi.ExecVariablePsiElement;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiElement;
import gnu.trove.THashSet;

public class StatementExecutionBasicProcessor implements StatementExecutionProcessor {

    protected DBLanguagePsiFile psiFile;
    protected ExecutablePsiElement cachedExecutable;

    protected String resultName;
    protected int index;

    private StatementExecutionInput executionInput;
    private StatementExecutionResult executionResult;

    public StatementExecutionBasicProcessor(ExecutablePsiElement psiElement, int index) {
        this.cachedExecutable = psiElement;
        this.psiFile = psiElement.getFile();
        this.index = index;
        executionInput = new StatementExecutionInput(psiElement.getText(), psiElement.prepareStatementText(), this);
    }

    public StatementExecutionBasicProcessor(DBLanguagePsiFile psiFile, String sqlStatement, int index) {
        this.psiFile = psiFile;
        this.index = index;
        sqlStatement = sqlStatement.trim();
        executionInput = new StatementExecutionInput(sqlStatement, sqlStatement, this);
    }

    public boolean isDirty(){
        if (getConnectionHandler() != executionInput.getConnectionHandler() || // connection changed since execution
                getCurrentSchema() != executionInput.getCurrentSchema()) { // current schema changed since execution)
            return true;

        } else {
            ExecutablePsiElement executablePsiElement = executionInput.getExecutablePsiElement();
            return this.cachedExecutable != null && !this.cachedExecutable.matches(executablePsiElement, BasePsiElement.MatchType.STRONG);
        }
    }

    @Override
    public void bind(ExecutablePsiElement executablePsiElement) {
        this.cachedExecutable = executablePsiElement;
        executablePsiElement.setExecutionProcessor(this);
    }

    @Override
    public void unbind() {
        cachedExecutable = null;
    }

    @Override
    public boolean isBound() {
        return cachedExecutable != null;
    }

    public DBLanguagePsiFile getPsiFile() {
        return psiFile;
    }

    @Override
    @Nullable
    public ExecutablePsiElement getCachedExecutable() {
        return cachedExecutable;
    }

    public static boolean contains(PsiElement parent, BasePsiElement childElement, BasePsiElement.MatchType matchType) {
        PsiElement child = parent.getFirstChild();
        while (child != null) {
            if (child == childElement) {
                return true;
            }
            if (child instanceof ChameleonPsiElement) {
                if (contains(child, childElement, matchType)) {
                    return true;
                }
            } else if(child instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) child;
                if (basePsiElement.matches(childElement, matchType)) {
                    return true;
                }
            }
            child = child.getNextSibling();
        }
        return false;
    }

    @Override
    public String toString() {
        return executionInput.getOriginalStatementText();
    }

    @Override
    public StatementExecutionInput getExecutionInput() {
        return executionInput;
    }

    public StatementExecutionResult getExecutionResult() {
        if (executionResult != null && executionResult.isDisposed()) {
            executionResult = null;
        }
        return executionResult;
    }

    public boolean promptVariablesDialog() {
        Set<ExecVariablePsiElement> bucket = new THashSet<ExecVariablePsiElement>();
        ExecutablePsiElement executablePsiElement = executionInput.getExecutablePsiElement();
        if (executablePsiElement != null) {
            executablePsiElement.collectExecVariablePsiElements(bucket);
        }

        StatementExecutionVariablesBundle executionVariables = executionInput.getExecutionVariables();
        if (bucket.isEmpty()) {
            executionVariables = null;
            executionInput.setExecutionVariables(null);
        } else {
            if (executionVariables == null)
                executionVariables = new StatementExecutionVariablesBundle(bucket); else
                executionVariables.initialize(bucket);
        }

        if (executionVariables != null) {
            StatementExecutionVariablesDialog dialog = new StatementExecutionVariablesDialog(this, executionInput.getExecutableStatementText());
            dialog.show();
            return dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE;
        }
        return true;
    }

    public void execute(ProgressIndicator progressIndicator) {
        progressIndicator.setText("Executing " + getStatementName());
        long startTimeMillis = System.currentTimeMillis();
        resultName = null;
        ConnectionHandler activeConnection = getConnectionHandler();
        DBSchema currentSchema = getCurrentSchema();

        boolean continueExecution = true;
        if (cachedExecutable != null) {
            executionInput.setOriginalStatementText(cachedExecutable.getText());
            executionInput.setExecutableStatementText(cachedExecutable.prepareStatementText());
        }


        String executableStatementText = executionInput.getExecutableStatementText();
        StatementExecutionVariablesBundle executionVariables = executionInput.getExecutionVariables();
        if (executionVariables != null) {
            executableStatementText = executionVariables.prepareStatementText(activeConnection, executableStatementText, false);
            executionInput.setExecutableStatementText(executableStatementText);

            if (executionVariables.hasErrors()) {
                executionResult = createErrorExecutionResult("Could not bind all variables.");
                continueExecution = false;
            }
        }

        Project project = getProject();
        if (continueExecution) {
            try {
                if (!activeConnection.isDisposed()) {
                    Connection connection = activeConnection.getStandaloneConnection(currentSchema);
                    Statement statement = connection.createStatement();

                    statement.setQueryTimeout(getStatementExecutionSettings().getExecutionTimeout());
                    statement.execute(executableStatementText);
                    executionResult = createExecutionResult(statement, executionInput);
                    ExecutablePsiElement executablePsiElement = executionInput.getExecutablePsiElement();
                    if (executablePsiElement != null) {
                        if (executablePsiElement.isTransactional()) activeConnection.notifyChanges(psiFile.getVirtualFile());
                        if (executablePsiElement.isTransactionControl()) activeConnection.resetChanges();
                    } else{
                        if (executionResult.getUpdateCount() > 0) activeConnection.notifyChanges(psiFile.getVirtualFile());
                    }


                    if (executionInput.isDataDefinitionStatement()) {
                        DBSchemaObject affectedObject = executionInput.getAffectedObject();
                        if (affectedObject != null) {
                            DataDefinitionChangeListener listener = EventManager.notify(project, DataDefinitionChangeListener.TOPIC);
                            listener.dataDefinitionChanged(affectedObject);
                        } else {
                            DBSchema affectedSchema = executionInput.getAffectedSchema();
                            IdentifierPsiElement subjectPsiElement = executionInput.getSubjectPsiElement();
                            if (affectedSchema != null && subjectPsiElement != null) {
                                DataDefinitionChangeListener listener = EventManager.notify(project, DataDefinitionChangeListener.TOPIC);
                                listener.dataDefinitionChanged(affectedSchema, subjectPsiElement.getObjectType());
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                executionResult = createErrorExecutionResult(e.getMessage());
            }
        }

        executionResult.setExecutionDuration((int) (System.currentTimeMillis() - startTimeMillis));
        ExecutionManager.getInstance(project).showExecutionConsole(executionResult);
    }

    public StatementExecutionVariablesBundle getExecutionVariables() {
        return executionInput.getExecutionVariables();
    }

    protected StatementExecutionResult createExecutionResult(Statement statement, final StatementExecutionInput executionInput) throws SQLException {
        final StatementExecutionBasicResult executionResult = new StatementExecutionBasicResult(this, getResultName(), statement.getUpdateCount());
        boolean isDdlStatement = executionInput.isDataDefinitionStatement();
        boolean hasCompilerErrors = false;
        if (isDdlStatement) {
            final BasePsiElement compilablePsiElement = executionInput.getCompilableBlockPsiElement();
            if (compilablePsiElement != null) {
                hasCompilerErrors = new ReadActionRunner<Boolean>() {
                    @Override
                    protected Boolean run() {
                        CompilerAction compilerAction = new CompilerAction(CompilerAction.Type.DDL, psiFile.getVirtualFile());
                        compilerAction.setStartOffset(compilablePsiElement.getTextOffset());
                        compilerAction.setContentType(executionInput.getCompilableContentType());
                        CompilerResult compilerResult = null;

                        DBSchemaObject underlyingObject = executionInput.getAffectedObject();
                        if (underlyingObject == null) {
                            ConnectionHandler connectionHandler = executionInput.getConnectionHandler();
                            DBSchema schema = executionInput.getAffectedSchema();
                            IdentifierPsiElement subjectPsiElement = executionInput.getSubjectPsiElement();
                            if (connectionHandler != null && schema != null && subjectPsiElement != null) {
                                DBObjectType objectType = subjectPsiElement.getObjectType();
                                String objectName = subjectPsiElement.getUnquotedText().toString().toUpperCase();
                                compilerResult = new CompilerResult(connectionHandler, schema, objectType, objectName, compilerAction);
                            }
                        } else {
                            compilerResult = new CompilerResult(underlyingObject, compilerAction);
                        }

                        if (compilerResult != null) {
                            executionResult.setCompilerResult(compilerResult);
                            return compilerResult.hasErrors();
                        }
                        return false;
                    }
                }.start();
            }
        }

        if (hasCompilerErrors) {
            String message = executionInput.getStatementDescription() + " executed with warnings";
            executionResult.updateExecutionMessage(MessageType.WARNING, message);
            executionResult.setExecutionStatus(StatementExecutionStatus.WARNING);

        } else {
            String message = executionInput.getStatementDescription() + " executed successfully";
            int updateCount = executionResult.getUpdateCount();
            if (!isDdlStatement && updateCount > -1) {
                message = message + ": " + updateCount + (updateCount != 1 ? " rows" : " row") + " affected";
            }
            executionResult.updateExecutionMessage(MessageType.INFO, message);
            executionResult.setExecutionStatus(StatementExecutionStatus.SUCCESS);
        }

        return executionResult;
    }



    public StatementExecutionResult createErrorExecutionResult(String cause) {
        StatementExecutionResult executionResult = new StatementExecutionBasicResult(this, getResultName(), 0);
        executionResult.updateExecutionMessage(MessageType.ERROR, "Could not execute " + getStatementName() + ".", cause);
        executionResult.setExecutionStatus(StatementExecutionStatus.ERROR);
        return executionResult;
    }

    public StatementExecutionSettings getStatementExecutionSettings() {
        return ExecutionEngineSettings.getInstance(getProject()).getStatementExecutionSettings();
    }

    public ConnectionHandler getConnectionHandler() {
        return psiFile.getActiveConnection();
    }

    public DBSchema getCurrentSchema() {
        return psiFile == null ? null : psiFile.getCurrentSchema();
    }

    public Project getProject() {
        return psiFile == null ? null : psiFile.getProject();
    }

    public synchronized String getResultName() {
        if (resultName == null) {
            ExecutablePsiElement executablePsiElement = executionInput.getExecutablePsiElement();
            if (executablePsiElement!= null) {
                 resultName = executablePsiElement.createSubjectList();
            }
            if (StringUtil.isEmptyOrSpaces(resultName)) {
                resultName = "Result " + index;
            }
        }
        return resultName;
    }

    public String getStatementName() {
        ExecutablePsiElement executablePsiElement = executionInput.getExecutablePsiElement();
        return executablePsiElement == null ? "SQL statement" : executablePsiElement.getElementType().getDescription();
    }

    public int getIndex() {
        return index;
    }

    public boolean canExecute() {
        return !isDisposed();
    }

    public void navigateToResult() {

    }

    public void navigateToEditor(boolean requestFocus) {
        if (cachedExecutable != null) {
            cachedExecutable.navigate(requestFocus);
        }
    }

    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    private boolean disposed;

    @Override
    public void dispose() {
        if (!isDisposed()) {
            disposed = true;
            cachedExecutable = null;
            psiFile = null;

        }
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }
}
