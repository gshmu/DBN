package com.dci.intellij.dbn.execution.statement.result;

import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetDataModel;
import com.dci.intellij.dbn.data.ui.table.resultSet.ResultSetTable;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.dci.intellij.dbn.execution.statement.options.StatementExecutionSettings;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionCursorProcessor;
import com.dci.intellij.dbn.execution.statement.result.ui.StatementExecutionResultForm;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.Disposer;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class StatementExecutionCursorResult extends StatementExecutionBasicResult {
    private StatementExecutionResultForm resultPanel;
    private ResultSetDataModel dataModel;

    public StatementExecutionCursorResult(
            String resultName,
            StatementExecutionInput executionInput,
            ResultSet resultSet) throws SQLException {
        super(resultName, executionInput);
        int fetchBlockSize = getQueryExecutionSettings().getResultSetFetchBlockSize();
        dataModel = new ResultSetDataModel(resultSet, executionInput.getConnectionHandler(), fetchBlockSize);
        resultPanel = new StatementExecutionResultForm(this);
        resultPanel.updateVisibleComponents();
        resultPanel.getResultTable().setName(getResultName());

        Disposer.register(this, dataModel);
    }

    private StatementExecutionSettings getQueryExecutionSettings() {
        ExecutionEngineSettings settings = ExecutionEngineSettings.getInstance(getProject());
        return settings.getStatementExecutionSettings();
    }

    public StatementExecutionCursorResult(
            String resultName,
            StatementExecutionInput executionInput) throws SQLException {
        super(resultName, executionInput);
    }

    public StatementExecutionCursorProcessor getExecutionProcessor() {
        return (StatementExecutionCursorProcessor) super.getExecutionProcessor();
    }

    public void reload() {
        new BackgroundTask(getProject(), "Reloading data", true) {
            public void execute(@NotNull ProgressIndicator progressIndicator) {
                initProgressIndicator(progressIndicator, true, "Reloading results for " + getExecutionProcessor().getStatementName());

                resultPanel.highlightLoading(true);
                long startTimeMillis = System.currentTimeMillis();
                try {
                    Connection connection = getConnectionHandler().getStandaloneConnection(getExecutionProcessor().getCurrentSchema());
                    Statement statement = connection.createStatement();
                    statement.setQueryTimeout(getQueryExecutionSettings().getExecutionTimeout());
                    statement.execute(getExecutionInput().getExecuteStatement());
                    ResultSet resultSet = statement.getResultSet();
                    loadResultSet(resultSet);
                } catch (final SQLException e) {
                    MessageUtil.showErrorDialog("Could not perform reload operation.", e);
                }
                setExecutionDuration((int) (System.currentTimeMillis() - startTimeMillis));
                resultPanel.highlightLoading(false);
            }
        }.start();
    }

    public void loadResultSet(ResultSet resultSet) throws SQLException {
        int rowCount = Math.max(dataModel.getRowCount() + 1, 100);
        dataModel = new ResultSetDataModel(resultSet, getConnectionHandler(), rowCount);
        resultPanel.reloadTableModel();
        resultPanel.updateVisibleComponents();
    }

    public StatementExecutionResultForm getResultPanel() {
        return resultPanel;
    }

    public void fetchNextRecords() {
        new BackgroundTask(getProject(), "Loading data", true) {
            public void execute(@NotNull ProgressIndicator progressIndicator) {
                initProgressIndicator(progressIndicator, true, "Loading next records for " + getExecutionProcessor().getStatementName());
                resultPanel.highlightLoading(true);
                try {
                    if (hasResult() && !dataModel.isResultSetExhausted()) {
                        int fetchBlockSize = getQueryExecutionSettings().getResultSetFetchBlockSize();
                        dataModel.fetchNextRecords(fetchBlockSize, false);
                        //tResult.accommodateColumnsSize();
                        if (dataModel.isResultSetExhausted()) {
                            dataModel.closeResultSet();
                        }
                        resultPanel.updateVisibleComponents();
                    }

                } catch (SQLException e) {
                    MessageUtil.showErrorDialog("Could not perform operation.", e);
                } finally {
                    resultPanel.highlightLoading(false);
                }

            }
        }.start();
    }

    public ResultSetDataModel getTableModel() {
        return dataModel;
    }

    @Nullable
    public ResultSetTable getResultTable() {
        return resultPanel == null ? null : resultPanel.getResultTable();
    }

    public boolean hasResult() {
        return dataModel != null;
    }

    public void navigateToResult() {
        if (resultPanel != null) {
            resultPanel.show();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        dataModel = null;
        resultPanel = null;
        dataProvider = null;
    }


    /********************************************************
     *                    Data Provider                     *
     ********************************************************/
    public DataProvider dataProvider = new DataProvider() {
        @Override
        public Object getData(@NonNls String dataId) {
            if (DBNDataKeys.STATEMENT_EXECUTION_RESULT.is(dataId)) {
                return StatementExecutionCursorResult.this;
            }
            if (PlatformDataKeys.PROJECT.is(dataId)) {
                return getProject();
            }
            return null;
        }
    };

    public DataProvider getDataProvider() {
        return dataProvider;
    }

}
