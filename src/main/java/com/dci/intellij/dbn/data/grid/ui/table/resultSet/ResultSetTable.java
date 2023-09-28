package com.dci.intellij.dbn.data.grid.ui.table.resultSet;

import com.dci.intellij.dbn.common.ui.component.DBNComponent;
import com.dci.intellij.dbn.common.ui.util.Borderless;
import com.dci.intellij.dbn.common.ui.util.Mouse;
import com.dci.intellij.dbn.common.util.Dialogs;
import com.dci.intellij.dbn.data.grid.ui.table.resultSet.record.ResultSetRecordViewerDialog;
import com.dci.intellij.dbn.data.grid.ui.table.sortable.SortableTable;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetDataModel;
import com.dci.intellij.dbn.data.record.RecordViewInfo;
import org.jetbrains.annotations.NotNull;

import java.awt.event.MouseEvent;

public class ResultSetTable<T extends ResultSetDataModel<?, ?>> extends SortableTable<T> implements Borderless {
    private final RecordViewInfo recordViewInfo;
    public ResultSetTable(DBNComponent parent, T dataModel, boolean enableSpeedSearch, RecordViewInfo recordViewInfo) {
        super(parent, dataModel, enableSpeedSearch);
        this.recordViewInfo = recordViewInfo;
        addMouseListener(Mouse.listener().onClick(e -> {
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                showRecordViewDialog();
            }}));
    }

    public RecordViewInfo getRecordViewInfo() {
        return recordViewInfo;
    }

    public void showRecordViewDialog() {
        Dialogs.show(() -> new ResultSetRecordViewerDialog(this, showRecordViewDataTypes()));
    }

    protected boolean showRecordViewDataTypes() {
        return true;
    }

    @NotNull
    @Override
    public T getModel() {
        return super.getModel();
    }
}
