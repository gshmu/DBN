package com.dci.intellij.dbn.execution.statement.result.ui;

import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.thread.ReadActionRunner;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.SessionId;
import com.dci.intellij.dbn.data.find.DataSearchComponent;
import com.dci.intellij.dbn.data.find.SearchableDataComponent;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTable;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTableScrollPane;
import com.dci.intellij.dbn.data.grid.ui.table.resultSet.ResultSetTable;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetDataModel;
import com.dci.intellij.dbn.data.record.RecordViewInfo;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.common.result.ui.ExecutionResultForm;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionCursorResult;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class StatementExecutionResultForm extends DBNFormImpl implements ExecutionResultForm<StatementExecutionCursorResult>, SearchableDataComponent {
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JPanel statusPanel;
    private JScrollPane resultScrollPane;
    private ResultSetTable resultTable;
    private JLabel statusLabel;
    private JPanel searchPanel;
    private JPanel resultPanel;
    private StatementExecutionCursorResult executionResult;
    private RecordViewInfo recordViewInfo;
    private DataSearchComponent dataSearchComponent;

    public StatementExecutionResultForm(final StatementExecutionCursorResult executionResult) {
        this.executionResult = executionResult;
        ActionToolbar actionToolbar = ActionUtil.createActionToolbar("", false, "DBNavigator.ActionGroup.StatementExecutionResult");
        actionToolbar.setTargetComponent(actionsPanel);

        actionsPanel.add(actionToolbar.getComponent());

        recordViewInfo = new ReadActionRunner<RecordViewInfo>() {
            @Override
            protected RecordViewInfo run() {
                return new RecordViewInfo(
                                executionResult.getName(),
                                executionResult.getIcon());
            }
        }.start();

        resultPanel.setBorder(IdeBorderFactory.createBorder());
        resultTable = new ResultSetTable(executionResult.getTableModel(), true, recordViewInfo);
        resultTable.setName(executionResult.getName());

        resultScrollPane.setViewportView(resultTable);
        resultScrollPane.getViewport().setBackground(resultTable.getBackground());
        resultTable.initTableGutter();

        JPanel panel = new JPanel();
        panel.setBorder(UIUtil.getTableHeaderCellBorder());
        resultScrollPane.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, panel);
        ActionUtil.registerDataProvider(mainPanel, executionResult);

        Disposer.register(this, executionResult);
        Disposer.register(this, resultTable);
    }

    public void setExecutionResult(StatementExecutionCursorResult executionResult) {
        if (this.executionResult != executionResult) {
            StatementExecutionCursorResult oldExecutionResult = this.executionResult;
            this.executionResult = executionResult;
            reloadTableModel();

            DisposerUtil.dispose(oldExecutionResult);
        }
    }

    @NotNull
    public StatementExecutionCursorResult getExecutionResult() {
        return FailsafeUtil.get(executionResult);
    }

    public void reloadTableModel() {
        SimpleLaterInvocator.invoke(() -> {
            StatementExecutionCursorResult executionResult = getExecutionResult();
            JScrollBar horizontalScrollBar = resultScrollPane.getHorizontalScrollBar();
            int horizontalScrolling = horizontalScrollBar.getValue();
            resultTable = new ResultSetTable(executionResult.getTableModel(), true, recordViewInfo);
            resultScrollPane.setViewportView(resultTable);
            resultTable.initTableGutter();
            resultTable.setName(StatementExecutionResultForm.this.executionResult.getName());
            horizontalScrollBar.setValue(horizontalScrolling);
        });
    }

    public ResultSetTable getResultTable() {
        return resultTable;
    }

    public void updateVisibleComponents() {
        new ConditionalLaterInvocator() {
            protected void execute() {
                StatementExecutionCursorResult executionResult = getExecutionResult();
                ResultSetDataModel dataModel = executionResult.getTableModel();
                String connectionName = executionResult.getConnectionHandler().getPresentableText();
                SessionId sessionId = executionResult.getExecutionInput().getTargetSessionId();
                String connectionType =
                        sessionId == SessionId.MAIN ? " (main)" :
                        sessionId == SessionId.POOL ? " (pool)" : " (session)";
                int rowCount = dataModel.getRowCount();
                String partialResultInfo = dataModel.isResultSetExhausted() ? "" : " (partial)";
                statusLabel.setText(connectionName + connectionType + ": " + rowCount + " records" + partialResultInfo);
            }
        }.start();

    }

    public void dispose() {
        super.dispose();
        executionResult = null;
    }

    public void show() {
        StatementExecutionCursorResult executionResult = getExecutionResult();
        Project project = executionResult.getProject();
        ExecutionManager.getInstance(project).selectResultTab(executionResult);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void highlightLoading(boolean loading) {
        resultTable.setLoading(loading);

        resultTable.revalidate();
        resultTable.repaint();
    }

    /*********************************************************
     *              SearchableDataComponent                  *
     *********************************************************/
    public void showSearchHeader() {
        resultTable.clearSelection();

        if (dataSearchComponent == null) {
            dataSearchComponent = new DataSearchComponent(this);
            ActionUtil.registerDataProvider(dataSearchComponent.getSearchField(), executionResult);
            searchPanel.add(dataSearchComponent, BorderLayout.CENTER);

            Disposer.register(this, dataSearchComponent);
        } else {
            dataSearchComponent.initializeFindModel();
        }
        if (searchPanel.isVisible()) {
            dataSearchComponent.getSearchField().selectAll();
        } else {
            searchPanel.setVisible(true);
        }
        dataSearchComponent.getSearchField().requestFocus();

    }

    public void hideSearchHeader() {
        dataSearchComponent.resetFindModel();
        searchPanel.setVisible(false);

        resultTable.revalidate();
        resultTable.repaint();
        resultTable.requestFocus();
    }

    @Override
    public void cancelEditActions() {
    }

    @Override
    public BasicTable getTable() {
        return resultTable;
    }

    @Override
    public String getSelectedText() {
        return null;
    }

    private void createUIComponents() {
        resultScrollPane = new BasicTableScrollPane();
    }
}
