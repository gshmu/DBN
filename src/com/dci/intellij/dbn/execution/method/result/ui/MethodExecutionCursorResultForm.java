package com.dci.intellij.dbn.execution.method.result.ui;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.DataProviderSupplier;
import com.dci.intellij.dbn.data.find.DataSearchComponent;
import com.dci.intellij.dbn.data.find.SearchableDataComponent;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTableScrollPane;
import com.dci.intellij.dbn.data.grid.ui.table.resultSet.ResultSetTable;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetDataModel;
import com.dci.intellij.dbn.data.record.RecordViewInfo;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.util.ui.UIUtil;

public class MethodExecutionCursorResultForm extends DBNFormImpl<MethodExecutionResultForm> implements SearchableDataComponent, DataProviderSupplier {
    private JPanel actionsPanel;
    private JScrollPane resultScrollPane;
    private JPanel mainPanel;
    private JPanel resultPanel;
    private JPanel searchPanel;

    private DBObjectRef<DBArgument> argumentRef;
    private ResultSetTable resultTable;
    private DataSearchComponent dataSearchComponent;
    private MethodExecutionResult executionResult;

    public MethodExecutionCursorResultForm(MethodExecutionResultForm parentComponent, MethodExecutionResult executionResult, DBArgument argument) {
        super(parentComponent);
        this.executionResult = executionResult;
        this.argumentRef = DBObjectRef.from(argument);
        ResultSetDataModel dataModel = executionResult.getTableModel(argument);
        RecordViewInfo recordViewInfo = new RecordViewInfo(
                executionResult.getName(),
                executionResult.getIcon());

        resultTable = new ResultSetTable(dataModel, true, recordViewInfo);
        resultTable.setPreferredScrollableViewportSize(new Dimension(500, -1));

        resultPanel.setBorder(IdeBorderFactory.createBorder());
        resultScrollPane.setViewportView(resultTable);
        resultScrollPane.getViewport().setBackground(resultTable.getBackground());
        resultTable.initTableGutter();

        JPanel panel = new JPanel();
        panel.setBorder(UIUtil.getTableHeaderCellBorder());
        resultScrollPane.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, panel);

        ActionToolbar actionToolbar = ActionUtil.createActionToolbar("", true, "DBNavigator.ActionGroup.MethodExecutionCursorResult");
        actionToolbar.setTargetComponent(actionsPanel);
        actionsPanel.add(actionToolbar.getComponent());
        ActionUtil.registerDataProvider(mainPanel, this);

        Disposer.register(this, resultTable);
    }

    public DBArgument getArgument() {
        return argumentRef.get();
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void dispose() {
        super.dispose();
        dataSearchComponent = null;
        resultTable = null;
    }

    private void createUIComponents() {
        resultScrollPane = new BasicTableScrollPane();
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
    public String getSelectedText() {
        return null;
    }

    @Override
    public ResultSetTable getTable() {
        return resultTable;
    }

    /********************************************************
     *                    Data Provider                     *
     ********************************************************/
    public DataProvider dataProvider = new DataProvider() {
        @Override
        public Object getData(@NonNls String dataId) {
            if (DBNDataKeys.METHOD_EXECUTION_CURSOR_RESULT_FORM.is(dataId)) {
                return MethodExecutionCursorResultForm.this;
            }
            if (DBNDataKeys.METHOD_EXECUTION_ARGUMENT.is(dataId)) {
                return DBObjectRef.get(argumentRef);
            }
            return null;
        }
    };

    @Nullable
    public DataProvider getDataProvider() {
        return dataProvider;
    }
}
