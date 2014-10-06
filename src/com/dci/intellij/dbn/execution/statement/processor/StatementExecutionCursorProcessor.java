package com.dci.intellij.dbn.execution.statement.processor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionBasicResult;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionCursorResult;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionResult;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionStatus;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;

public class StatementExecutionCursorProcessor extends StatementExecutionBasicProcessor {

    public StatementExecutionCursorProcessor(ExecutablePsiElement psiElement, int index) {
        super(psiElement, index);
    }

    public StatementExecutionCursorProcessor(DBLanguagePsiFile file, String sqlStatement, int index) {
        super(file, sqlStatement,  index);
    }

    protected StatementExecutionResult createExecutionResult(Statement statement, StatementExecutionInput executionInput) throws SQLException {
        ResultSet resultSet = statement.getResultSet();
        int updateCount = statement.getUpdateCount();
        String resultName = getResultName();
        if (resultSet == null) {
            statement.close();

            StatementExecutionResult executionResult = new StatementExecutionCursorResult(executionInput, resultName, updateCount);
            executionResult.updateExecutionMessage(MessageType.INFO, getStatementName() + " executed successfully.");
            executionResult.setExecutionStatus(StatementExecutionStatus.SUCCESS);
            return executionResult;
        } else {
            StatementExecutionBasicResult executionResult = getExecutionResult();
            if (executionResult == null) {
                executionResult = new StatementExecutionCursorResult(executionInput, resultName, resultSet, updateCount);
                executionResult.setExecutionStatus(StatementExecutionStatus.SUCCESS);
                return executionResult;
            } else {
                // if executionResult exists, just update it with the new resultSet data
                if (executionResult instanceof StatementExecutionCursorResult){
                    StatementExecutionCursorResult executionCursorResult = (StatementExecutionCursorResult) executionResult;
                    executionCursorResult.setExecutionInput(executionInput);
                    executionCursorResult.loadResultSet(resultSet);
                    return executionResult;
                } else {
                    return new StatementExecutionCursorResult(executionInput, resultName, resultSet, updateCount);
                }
            }
        }

    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean canExecute() {
        if (super.canExecute()) {
            StatementExecutionBasicResult executionResult = getExecutionResult();
            return executionResult == null ||
                    executionResult.getExecutionStatus() == StatementExecutionStatus.ERROR ||
                    executionResult.getExecutionInput().isObsolete();
        }
        return false;
    }

    public void navigateToResult() {
        StatementExecutionBasicResult executionResult = getExecutionResult();
        if (executionResult instanceof StatementExecutionCursorResult) {
            StatementExecutionCursorResult executionCursorResult = (StatementExecutionCursorResult) executionResult;
            executionCursorResult.navigateToResult();
        }

    }
}
