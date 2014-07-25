package com.dci.intellij.dbn.data.grid.ui.table.sortable;

import javax.swing.*;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTable;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTableSpeedSearch;
import com.dci.intellij.dbn.data.model.ColumnInfo;
import com.dci.intellij.dbn.data.model.sortable.SortableDataModel;
import com.dci.intellij.dbn.data.model.sortable.SortableTableHeaderMouseListener;
import com.dci.intellij.dbn.data.model.sortable.SortableTableMouseListener;
import com.dci.intellij.dbn.data.sorting.SortDirection;
import com.intellij.openapi.diagnostic.Logger;

public abstract class SortableTable<T extends SortableDataModel> extends BasicTable<T> {
    protected Logger logger = LoggerFactory.createLogger();

    public SortableTable(T dataModel, boolean enableSpeedSearch) {
        super(dataModel.getProject(), dataModel);
        addMouseListener(new SortableTableMouseListener(this));
        getTableHeader().setDefaultRenderer(new SortableTableHeaderRenderer());
        getTableHeader().addMouseListener(new SortableTableHeaderMouseListener(this));

        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setCellSelectionEnabled(true);
        accommodateColumnsSize();
        if (enableSpeedSearch) {
            new BasicTableSpeedSearch(this);
        }
    }

    public void sort() {
        getModel().sort();
    }

    public boolean sort(int columnIndex, SortDirection sortDirection, boolean keepExisting) {
        SortableDataModel model = getModel();
        int modelColumnIndex = convertColumnIndexToModel(columnIndex);
        ColumnInfo columnInfo = getModel().getColumnInfo(modelColumnIndex);
        if (columnInfo.isSortable()) {
            boolean sorted = model.sort(modelColumnIndex, sortDirection, keepExisting);
            if (sorted) getTableHeader().repaint();
            return sorted;
        }
        return false;
    }

}
