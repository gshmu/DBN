package com.dci.intellij.dbn.execution.statement.processor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.compiler.CompilerAction;
import com.dci.intellij.dbn.execution.compiler.CompilerResult;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.dci.intellij.dbn.execution.statement.options.StatementExecutionSettings;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionBasicResult;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionResult;
import com.dci.intellij.dbn.execution.statement.variables.StatementExecutionVariablesBundle;
import com.dci.intellij.dbn.execution.statement.variables.ui.StatementExecutionVariablesDialog;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.ExecVariablePsiElement;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.NamedPsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import gnu.trove.THashSet;

public class StatementExecutionBasicProcessor implements StatementExecutionProcessor {

    protected StatementExecutionVariablesBundle executionVariables;
    protected ExecutablePsiElement executablePsiElement;
    protected String executableStatement;
    protected DBLanguagePsiFile file;


    protected String resultName;
    protected int index;

    private StatementExecutionResult executionResult;

    public StatementExecutionBasicProcessor(ExecutablePsiElement psiElement, int index) {
        this.executablePsiElement = psiElement;
        this.file = psiElement.getFile();
        this.index = index;

    }

    public StatementExecutionBasicProcessor(DBLanguagePsiFile file, String sqlStatement, int index) {
        this.executableStatement = sqlStatement.trim();
        this.file = file;
        this.index = index;
    }

    public void bind(ExecutablePsiElement executablePsiElement) {
        this.executablePsiElement = executablePsiElement;
    }

    public boolean matches(ExecutablePsiElement executablePsiElement, boolean lenient) {
        if (this.executablePsiElement != null && executablePsiElement.getFile().equals(file)) {
            StatementExecutionBasicResult executionResult = getExecutionResult();
            if (executionResult == null) {
                return lenient ?
                        this.executablePsiElement.matches(executablePsiElement) :
                        this.executablePsiElement.equals(executablePsiElement);
            } else {
                StatementExecutionInput executionInput = executionResult.getExecutionInput();
                return lenient || executionInput == null ?
                        this.executablePsiElement.matches(executablePsiElement) :
                        executionInput.getExecutablePsiElement().matches(executablePsiElement);
            }
        }

        return false;
    }

    public boolean isOrphan(){
        if (executablePsiElement == null || !executablePsiElement.isValid()) return true;
        PsiFile psiFile = PsiUtil.getPsiFile(getProject(), file.getVirtualFile());
        if (!psiFile.equals(file)) return true;

        NamedPsiElement rootPsiElement = executablePsiElement.lookupEnclosingRootPsiElement();
        return rootPsiElement == null || !PsiUtil.contains(file, rootPsiElement, true);
    }

    @Override
    public String toString() {
        return executablePsiElement.getText();
    }

    public boolean isDirty() {
        return executablePsiElement == null || !executablePsiElement.isValid();
    }

    public StatementExecutionBasicResult getExecutionResult() {
        if (executionResult != null && executionResult.isDisposed()) {
            executionResult = null;
        }
        return (StatementExecutionBasicResult) executionResult;
    }

    public boolean promptVariablesDialog() {
        Set<ExecVariablePsiElement> bucket = new THashSet<ExecVariablePsiElement>();
        if (executablePsiElement != null) {
            executablePsiElement.collectExecVariablePsiElements(bucket);
        }

        if (bucket.isEmpty()) {
            executionVariables = null;
        } else {
            if (executionVariables == null)
                executionVariables = new StatementExecutionVariablesBundle(getConnectionHandler(), getCurrentSchema(), bucket); else
                executionVariables.initialize(bucket);
        }

        if (executionVariables != null) {
            StatementExecutionVariablesDialog dialog = new StatementExecutionVariablesDialog(executablePsiElement.getProject(), executionVariables, executablePsiElement.getText());
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
        String originalStatementText = executablePsiElement == null ? executableStatement : executablePsiElement.getText();
        String executeStatementText = executablePsiElement == null ? executableStatement : executablePsiElement.prepareStatementText();

        StatementExecutionInput executionInput = new StatementExecutionInput(originalStatementText, executeStatementText, this);
        boolean continueExecution = true;

        if (executionVariables != null) {
            executeStatementText = executionVariables.prepareStatementText(activeConnection, executeStatementText, false);
            executionInput.setExecuteStatement(executeStatementText);

            if (executionVariables.hasErrors()) {
                executionResult = createErrorExecutionResult(executionInput, "Could not bind all variables.");
                continueExecution = false;
            }
        }

        if (continueExecution) {
            try {
                if (!activeConnection.isDisposed()) {
                    Connection connection = activeConnection.getStandaloneConnection(currentSchema);
                    Statement statement = connection.createStatement();

                    statement.setQueryTimeout(getStatementExecutionSettings().getExecutionTimeout());
                    statement.execute(executeStatementText);
                    executionResult = createExecutionResult(statement, executionInput);
                    if (executablePsiElement != null) {
                        if (executablePsiElement.isTransactional()) activeConnection.notifyChanges(file.getVirtualFile());
                        if (executablePsiElement.isTransactionControl()) activeConnection.resetChanges();
                    } else{
                        if (executionResult.getUpdateCount() > 0) activeConnection.notifyChanges(file.getVirtualFile());
                    }

                }
            } catch (SQLException e) {
                executionResult = createErrorExecutionResult(executionInput, e.getMessage());
            }
        }

        executionResult.setExecutionDuration((int) (System.currentTimeMillis() - startTimeMillis));
        ExecutionManager.getInstance(getProject()).showExecutionConsole(executionResult);
    }

    public StatementExecutionVariablesBundle getExecutionVariables() {
        return executionVariables;
    }

    protected StatementExecutionResult createExecutionResult(Statement statement, StatementExecutionInput executionInput) throws SQLException {
        StatementExecutionBasicResult executionResult = new StatementExecutionBasicResult(executionInput, getResultName(), statement.getUpdateCount());
        String message = executablePsiElement.getPresentableText() + " executed successfully";
        int updateCount = executionResult.getUpdateCount();
        boolean isDdlStatement = executionInput.isDDLStatement();
        if (!isDdlStatement && updateCount > -1) {
            message = message + ": " + updateCount + (updateCount != 1 ? " rows" : " row") + " affected";
        }
        executionResult.updateExecutionMessage(MessageType.INFO, message);
        executionResult.setExecutionStatus(StatementExecutionResult.STATUS_SUCCESS);
        if (isDdlStatement) {
            StatementExecutionProcessor executionProcessor = executionInput.getExecutionProcessor();
            if (executionProcessor != null && !executionProcessor.isDisposed()) {
                ExecutablePsiElement executablePsiElement = executionProcessor.getExecutablePsiElement();
                if (executablePsiElement != null) {
                    IdentifierPsiElement subjectPsiElement = (IdentifierPsiElement) executablePsiElement.lookupFirstPsiElement(ElementTypeAttribute.SUBJECT);
                    if (subjectPsiElement != null) {
                        DBObject object = subjectPsiElement.resolveUnderlyingObject();
                        if (object != null && object instanceof DBSchemaObject) {

                            VirtualFile virtualFile = executablePsiElement.getFile().getVirtualFile();
                            CompilerResult compilerResult = new CompilerResult((DBSchemaObject) object, new CompilerAction(CompilerAction.Type.DDL, virtualFile));
                            executionResult.setCompilerResult(compilerResult);
                        }
                    }
                }
            }
        }
        return executionResult;
    }

    public StatementExecutionResult createErrorExecutionResult(StatementExecutionInput executionInput, String cause) {
        StatementExecutionResult executionResult = new StatementExecutionBasicResult(executionInput, getResultName(), 0);
        executionResult.updateExecutionMessage(MessageType.ERROR, "Could not execute " + getStatementName() + ".", cause);
        executionResult.setExecutionStatus(StatementExecutionResult.STATUS_ERROR);
        return executionResult;
    }

    public StatementExecutionSettings getStatementExecutionSettings() {
        return ExecutionEngineSettings.getInstance(getProject()).getStatementExecutionSettings();
    }

    public ConnectionHandler getConnectionHandler() {
        return file.getActiveConnection();
    }

    public DBSchema getCurrentSchema() {
        return file == null ? null : file.getCurrentSchema();
    }

    public ExecutablePsiElement getExecutablePsiElement() {
        return executablePsiElement;
    }

    public Project getProject() {
        return file == null ? null : file.getProject();
    }

    public DBLanguagePsiFile getFile() {
        return file;
    }

    public synchronized String getResultName() {
        if (resultName == null) {
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
        if (executablePsiElement != null) {
            executablePsiElement.navigate(requestFocus);
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
            executablePsiElement = null;
            file = null;

        }
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }
}
