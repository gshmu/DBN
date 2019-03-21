package com.dci.intellij.dbn.execution.method.result.ui;

import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.GUIUtil;
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
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class MethodExecutionCursorResultForm extends DBNFormImpl<MethodExecutionResultForm> implements SearchableDataComponent, DataProviderSupplier {
    private JPanel actionsPanel;
    private JScrollPane resultScrollPane;
    private JPanel mainPanel;
    private JPanel resultPanel;
    private JPanel searchPanel;

    private DBObjectRef<DBArgument> argumentRef;
    private ResultSetTable resultTable;
    private MethodExecutionResult executionResult;

    private Latent<DataSearchComponent> dataSearchComponent = Latent.disposable(this, () -> {
        DataSearchComponent dataSearchComponent = new DataSearchComponent(MethodExecutionCursorResultForm.this);
        searchPanel.add(dataSearchComponent.getComponent(), BorderLayout.CENTER);
        ActionUtil.registerDataProvider(dataSearchComponent.getSearchField(), executionResult);
        return dataSearchComponent;
    });


    MethodExecutionCursorResultForm(MethodExecutionResultForm parentComponent, MethodExecutionResult executionResult, DBArgument argument) {
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

    @NotNull
    @Override
    public JPanel getComponent() {
        return mainPanel;
    }

    private void createUIComponents() {
        resultScrollPane = new BasicTableScrollPane();
    }

    /*********************************************************
     *              SearchableDataComponent                  *
     *********************************************************/
    @Override
    public void showSearchHeader() {
        resultTable.clearSelection();

        DataSearchComponent dataSearchComponent = getSearchComponent();
        dataSearchComponent.initializeFindModel();
        JTextField searchField = dataSearchComponent.getSearchField();
        if (searchPanel.isVisible()) {
            searchField.selectAll();
        } else {
            searchPanel.setVisible(true);
        }
        searchField.requestFocus();

    }

    private DataSearchComponent getSearchComponent() {
        return dataSearchComponent.get();
    }

    @Override
    public void hideSearchHeader() {
        getSearchComponent().resetFindModel();
        searchPanel.setVisible(false);
        GUIUtil.repaintAndFocus(resultTable);
    }

    @Override
    public void cancelEditActions() {

    }

    @Override
    public String getSelectedText() {
        return null;
    }

    @NotNull
    @Override
    public ResultSetTable getTable() {
        return Failsafe.get(resultTable);
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

    @Override
    @Nullable
    public DataProvider getDataProvider() {
        return dataProvider;
    }
}
