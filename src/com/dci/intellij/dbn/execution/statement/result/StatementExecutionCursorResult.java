package com.dci.intellij.dbn.execution.statement.result;

import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.DBNResultSet;
import com.dci.intellij.dbn.connection.jdbc.DBNStatement;
import com.dci.intellij.dbn.data.grid.ui.table.resultSet.ResultSetTable;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetDataModel;
import com.dci.intellij.dbn.execution.ExecutionContext;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.dci.intellij.dbn.execution.statement.options.StatementExecutionSettings;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionCursorProcessor;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.execution.statement.result.ui.StatementExecutionResultForm;
import com.dci.intellij.dbn.object.DBSchema;
import com.intellij.openapi.actionSystem.DataProvider;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

import static com.dci.intellij.dbn.execution.ExecutionStatus.EXECUTING;

public class StatementExecutionCursorResult extends StatementExecutionBasicResult {
    private StatementExecutionResultForm resultPanel;
    private ResultSetDataModel dataModel;

    public StatementExecutionCursorResult(
            @NotNull StatementExecutionProcessor executionProcessor,
            @NotNull String resultName,
            DBNResultSet resultSet,
            int updateCount) throws SQLException {
        super(executionProcessor, resultName, updateCount);
        int fetchBlockSize = getQueryExecutionSettings().getResultSetFetchBlockSize();
        dataModel = new ResultSetDataModel(resultSet, executionProcessor.getConnectionHandler(), fetchBlockSize);
        resultPanel = new StatementExecutionResultForm(this);
        resultPanel.updateVisibleComponents();
    }

    private StatementExecutionSettings getQueryExecutionSettings() {
        ExecutionEngineSettings settings = ExecutionEngineSettings.getInstance(getProject());
        return settings.getStatementExecutionSettings();
    }

    public StatementExecutionCursorResult(
            StatementExecutionProcessor executionProcessor,
            @NotNull String resultName,
            int updateCount) throws SQLException {
        super(executionProcessor, resultName, updateCount);
    }

    @NotNull
    public StatementExecutionCursorProcessor getExecutionProcessor() {
        return (StatementExecutionCursorProcessor) super.getExecutionProcessor();
    }

    public void reload() {
        BackgroundTask.invoke(getProject(), "Reloading data", true, false, (task, progress) -> {
            BackgroundTask.initProgressIndicator(progress, true, "Reloading results for " + getExecutionProcessor().getStatementName());
            ExecutionContext context = getExecutionProcessor().getExecutionContext(true);
            context.setExecutionTimestamp(System.currentTimeMillis());
            context.set(EXECUTING, true);

            try {
                resultPanel.highlightLoading(true);
                StatementExecutionInput executionInput = getExecutionInput();
                try {
                    ConnectionHandler connectionHandler = getConnectionHandler();
                    DBSchema currentSchema = getDatabaseSchema();
                    DBNConnection connection = connectionHandler.getMainConnection(currentSchema);
                    DBNStatement statement = connection.createStatement();
                    statement.setQueryTimeout(executionInput.getExecutionTimeout());
                    statement.execute(executionInput.getExecutableStatementText());
                    DBNResultSet resultSet = statement.getResultSet();
                    if (resultSet != null) {
                        loadResultSet(resultSet);
                    }
                } catch (final SQLException e) {
                    MessageUtil.showErrorDialog(getProject(), "Could not perform reload operation.", e);
                }
            } finally {
                calculateExecDuration();
                resultPanel.highlightLoading(false);
                context.reset();
            }
        });
    }

    public void loadResultSet(DBNResultSet resultSet) throws SQLException {
        StatementExecutionResultForm resultPanel = FailsafeUtil.get(this.resultPanel);
        int rowCount = Math.max(dataModel == null ? 0 : dataModel.getRowCount() + 1, 100);
        dataModel = new ResultSetDataModel(resultSet, getConnectionHandler(), rowCount);
        resultPanel.reloadTableModel();
        resultPanel.updateVisibleComponents();
    }

    public StatementExecutionResultForm getForm(boolean create) {
        return resultPanel;
    }

    public void fetchNextRecords() {
        BackgroundTask.invoke(getProject(), "Loading data", true, false, (task, progress) -> {
            BackgroundTask.initProgressIndicator(progress, true, "Loading next records for " + getExecutionProcessor().getStatementName());
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
                MessageUtil.showErrorDialog(getProject(), "Could not perform operation.", e);
            } finally {
                resultPanel.highlightLoading(false);
            }
        });
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
        DisposerUtil.disposeInBackground(dataModel);
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
            if (DBNDataKeys.STATEMENT_EXECUTION_CURSOR_RESULT.is(dataId)) {
                return StatementExecutionCursorResult.this;
            }
            return null;
        }
    };

    @Nullable
    public DataProvider getDataProvider() {
        return dataProvider;
    }

}
