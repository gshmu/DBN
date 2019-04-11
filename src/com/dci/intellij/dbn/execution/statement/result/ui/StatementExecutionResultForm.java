package com.dci.intellij.dbn.execution.statement.result.ui;

import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.thread.Read;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.SessionId;
import com.dci.intellij.dbn.data.find.DataSearchComponent;
import com.dci.intellij.dbn.data.find.SearchableDataComponent;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTable;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTableScrollPane;
import com.dci.intellij.dbn.data.grid.ui.table.resultSet.ResultSetTable;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetDataModel;
import com.dci.intellij.dbn.data.record.RecordViewInfo;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.common.result.ui.ExecutionResultFormBase;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionCursorResult;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class StatementExecutionResultForm extends ExecutionResultFormBase<StatementExecutionCursorResult> implements SearchableDataComponent {
    private JScrollPane resultScrollPane;
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JPanel statusPanel;
    private JPanel searchPanel;
    private JPanel resultPanel;
    private JLabel statusLabel;
    private ResultSetTable<ResultSetDataModel> resultTable;
    private RecordViewInfo recordViewInfo;

    private Latent<DataSearchComponent> dataSearchComponent = Latent.disposable(this, () -> {
        DataSearchComponent dataSearchComponent = new DataSearchComponent(StatementExecutionResultForm.this);
        searchPanel.add(dataSearchComponent.getComponent(), BorderLayout.CENTER);
        DataManager.registerDataProvider(dataSearchComponent.getSearchField(), this);
        return dataSearchComponent;
    });

    public StatementExecutionResultForm(StatementExecutionCursorResult executionResult) {
        super(executionResult);
        ActionToolbar actionToolbar = ActionUtil.createActionToolbar("", false, "DBNavigator.ActionGroup.StatementExecutionResult");
        actionToolbar.setTargetComponent(actionsPanel);

        actionsPanel.add(actionToolbar.getComponent());

        recordViewInfo = Read.call(() ->
                new RecordViewInfo(
                    executionResult.getName(),
                    executionResult.getIcon()));

        resultPanel.setBorder(IdeBorderFactory.createBorder());
        resultTable = new ResultSetTable<>(executionResult.getTableModel(), true, recordViewInfo);
        resultTable.setName(executionResult.getName());

        resultScrollPane.setViewportView(resultTable);
        resultScrollPane.getViewport().setBackground(resultTable.getBackground());
        resultTable.initTableGutter();

        JPanel panel = new JPanel();
        panel.setBorder(UIUtil.getTableHeaderCellBorder());
        resultScrollPane.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, panel);

        Disposer.register(this, executionResult);
        Disposer.register(this, resultTable);
    }

    public void rebuildForm() {
        Dispatch.invoke(() -> {
            StatementExecutionCursorResult executionResult = getExecutionResult();
            JScrollBar horizontalScrollBar = resultScrollPane.getHorizontalScrollBar();
            int horizontalScrolling = horizontalScrollBar.getValue();
            resultTable = Disposer.replace(resultTable, new ResultSetTable<>(executionResult.getTableModel(), true, recordViewInfo));
            resultScrollPane.setViewportView(resultTable);
            resultTable.initTableGutter();
            resultTable.setName(getExecutionResult().getName());
            horizontalScrollBar.setValue(horizontalScrolling);
        });
    }

    @NotNull
    public ResultSetTable getResultTable() {
        return Failsafe.nn(resultTable);
    }

    public void updateVisibleComponents() {
        Dispatch.invoke(() -> {
            StatementExecutionCursorResult executionResult = getExecutionResult();
            ResultSetDataModel dataModel = executionResult.getTableModel();
            ConnectionHandler connectionHandler = executionResult.getConnectionHandler();
            String connectionName = connectionHandler.getPresentableText();
            SessionId sessionId = executionResult.getExecutionInput().getTargetSessionId();
            String connectionType =
                    sessionId == SessionId.MAIN ? " (main)" :
                    sessionId == SessionId.POOL ? " (pool)" : " (session)";
            int rowCount = dataModel.getRowCount();
            String partialResultInfo = dataModel.isResultSetExhausted() ? "" : " (partial)";
            statusLabel.setText(connectionName + connectionType + ": " + rowCount + " records" + partialResultInfo);
            statusLabel.setIcon(connectionHandler.getIcon());
        });
    }

    public void show() {
        StatementExecutionCursorResult executionResult = getExecutionResult();
        Project project = executionResult.getProject();
        ExecutionManager.getInstance(project).selectResultTab(executionResult);
    }

    @NotNull
    @Override
    public JPanel ensureComponent() {
        return mainPanel;
    }

    public void highlightLoading(boolean loading) {
        ResultSetTable resultTable = getResultTable();
        resultTable.setLoading(loading);
        GUIUtil.repaint(resultTable);
    }

    /*********************************************************
     *              SearchableDataComponent                  *
     *********************************************************/
    @Override
    public void showSearchHeader() {
        getResultTable().clearSelection();

        DataSearchComponent dataSearchComponent = getSearchComponent();
        dataSearchComponent.initializeFindModel();
        if (searchPanel.isVisible()) {
            dataSearchComponent.getSearchField().selectAll();
        } else {
            searchPanel.setVisible(true);
        }
        dataSearchComponent.getSearchField().requestFocus();

    }

    private DataSearchComponent getSearchComponent() {
        return dataSearchComponent.get();
    }

    @Override
    public void hideSearchHeader() {
        getSearchComponent().resetFindModel();
        searchPanel.setVisible(false);
        GUIUtil.repaintAndFocus(getResultTable());
    }

    @Override
    public void cancelEditActions() {
    }

    @NotNull
    @Override
    public BasicTable getTable() {
        return getResultTable();
    }

    @Override
    public String getSelectedText() {
        return null;
    }

    private void createUIComponents() {
        resultScrollPane = new BasicTableScrollPane();
    }
}
