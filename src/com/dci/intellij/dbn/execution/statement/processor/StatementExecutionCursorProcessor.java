package com.dci.intellij.dbn.execution.statement.processor;

import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionBasicResult;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionCursorResult;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionResult;
import com.dci.intellij.dbn.language.common.DBLanguageFile;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class StatementExecutionCursorProcessor extends StatementExecutionBasicProcessor {

    public StatementExecutionCursorProcessor(ExecutablePsiElement psiElement, int index) {
        super(psiElement, index);
    }

    public StatementExecutionCursorProcessor(DBLanguageFile file, String sqlStatement, int index) {
        super(file, sqlStatement,  index);
    }

    protected StatementExecutionResult createExecutionResult(Statement statement, StatementExecutionInput executionInput) throws SQLException {
        ResultSet resultSet = statement.getResultSet();
        if (resultSet == null) {
            statement.close();

            StatementExecutionResult executionResult = new StatementExecutionCursorResult(getResultName(), executionInput);
            executionResult.updateExecutionMessage(MessageType.INFO, getStatementName() + " executed successfully.");
            return executionResult;
        } else {
            StatementExecutionBasicResult executionResult = getExecutionResult();
            if (executionResult == null) {
                return new StatementExecutionCursorResult(getResultName(), executionInput, resultSet);
            } else {
                // if executionResult exists, just update it with the new resultSet data
                if (executionResult instanceof StatementExecutionCursorResult){
                    StatementExecutionCursorResult executionCursorResult = (StatementExecutionCursorResult) executionResult;
                    executionCursorResult.setExecutionInput(executionInput);
                    executionCursorResult.loadResultSet(resultSet);
                    return executionResult;
                } else {
                    return new StatementExecutionCursorResult(getResultName(), executionInput, resultSet);
                }
            }
        }
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean canExecute() {
        StatementExecutionBasicResult executionResult = getExecutionResult();
        return executionResult == null ||
                executionResult.getExecutionStatus() == StatementExecutionResult.STATUS_ERROR ||
                executionResult.getExecutionInput().isObsolete() || isDirty();
    }

    public void navigateToResult() {
        StatementExecutionBasicResult executionResult = getExecutionResult();
        if (executionResult instanceof StatementExecutionCursorResult) {
            StatementExecutionCursorResult executionCursorResult = (StatementExecutionCursorResult) executionResult;
            executionCursorResult.navigateToResult();
        }

    }
}
