package com.dci.intellij.dbn.execution.method.result.ui;

import com.dci.intellij.dbn.common.action.DataKeys;
import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.latent.RuntimeLatent;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.data.find.DataSearchComponent;
import com.dci.intellij.dbn.data.find.SearchableDataComponent;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTableScrollPane;
import com.dci.intellij.dbn.data.grid.ui.table.resultSet.ResultSetTable;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetDataModel;
import com.dci.intellij.dbn.data.record.RecordViewInfo;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class MethodExecutionCursorResultForm extends DBNFormImpl<MethodExecutionResultForm> implements SearchableDataComponent {
    private JPanel actionsPanel;
    private JScrollPane resultScrollPane;
    private JPanel mainPanel;
    private JPanel resultPanel;
    private JPanel searchPanel;

    private DBObjectRef<DBArgument> argumentRef;
    private ResultSetTable resultTable;
    private MethodExecutionResult executionResult;

    private RuntimeLatent<DataSearchComponent> dataSearchComponent = Latent.disposable(this, () -> {
        DataSearchComponent dataSearchComponent = new DataSearchComponent(MethodExecutionCursorResultForm.this);
        searchPanel.add(dataSearchComponent.getComponent(), BorderLayout.CENTER);
        DataManager.registerDataProvider(dataSearchComponent.getSearchField(), this);
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
        DataManager.registerDataProvider(actionToolbar.getComponent(), this);

        Disposer.register(this, resultTable);
    }

    public DBArgument getArgument() {
        return argumentRef.get();
    }

    @NotNull
    @Override
    public JPanel ensureComponent() {
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
        return Failsafe.nn(resultTable);
    }

    /********************************************************
     *                    Data Provider                     *
     ********************************************************/
    @Nullable
    @Override
    public Object getData(@NotNull String dataId) {
        if (DataKeys.METHOD_EXECUTION_CURSOR_RESULT_FORM.is(dataId)) {
            return MethodExecutionCursorResultForm.this;
        }
        if (DataKeys.METHOD_EXECUTION_ARGUMENT.is(dataId)) {
            return DBObjectRef.get(argumentRef);
        }
        return null;
    }
}
