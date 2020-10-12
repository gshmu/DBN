package com.dci.intellij.dbn.editor.data.state.column.ui;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.ui.list.CheckBoxList;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.dci.intellij.dbn.editor.data.state.column.DatasetColumnSetup;
import com.dci.intellij.dbn.editor.data.state.column.DatasetColumnState;
import com.dci.intellij.dbn.editor.data.state.column.action.MoveDownAction;
import com.dci.intellij.dbn.editor.data.state.column.action.MoveUpAction;
import com.dci.intellij.dbn.editor.data.state.column.action.OrderAlphabeticallyAction;
import com.dci.intellij.dbn.editor.data.state.column.action.RevertColumnOrderAction;
import com.dci.intellij.dbn.editor.data.state.column.action.SelectAllColumnsAction;
import com.dci.intellij.dbn.object.DBDataset;
import com.intellij.openapi.actionSystem.ActionToolbar;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatasetColumnSetupForm extends DBNFormImpl {
    private JPanel mainPanel;
    private JPanel actionPanel;
    private JScrollPane columnListScrollPane;
    private JPanel headerPanel;
    private final CheckBoxList<ColumnStateSelectable> columnList;
    private final DatasetColumnSetup columnSetup;

    public DatasetColumnSetupForm(@NotNull DatasetEditor datasetEditor) {
        super(datasetEditor.getProject());
        DBDataset dataset = datasetEditor.getDataset();
        columnSetup = datasetEditor.getColumnSetup();
        List<DatasetColumnState> columnStates = columnSetup.getColumnStates();
        List<ColumnStateSelectable> columnStateSel = new ArrayList<ColumnStateSelectable>();
        for (DatasetColumnState columnState : columnStates) {
            columnStateSel.add(new ColumnStateSelectable(dataset, columnState));
        }

        columnList = new CheckBoxList<>(columnStateSel, true);
        columnListScrollPane.setViewportView(columnList);

        ActionToolbar actionToolbar = ActionUtil.createActionToolbar("", false,
                new SelectAllColumnsAction(columnList),
                ActionUtil.SEPARATOR,
                new MoveUpAction(columnList),
                new MoveDownAction(columnList),
                ActionUtil.SEPARATOR,
                new OrderAlphabeticallyAction(columnList),
                new RevertColumnOrderAction(columnList));
        actionPanel.add(actionToolbar.getComponent(), BorderLayout.WEST);

        createHeaderForm(dataset);
    }

    private void createHeaderForm(DBDataset dataset) {
        DBNHeaderForm headerForm = new DBNHeaderForm(this, dataset);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    public boolean applyChanges(){
        boolean changed = columnList.applyChanges();
        ListModel model = columnList.getModel();
        for(int i=0; i<model.getSize(); i++ ) {
            ColumnStateSelectable columnState = columnList.getElementAt(i);
            changed = changed || columnState.getPosition() != i;
            columnState.setPosition((short) i);
        }
        Collections.sort(columnSetup.getColumnStates());
        return changed;
    }

}
