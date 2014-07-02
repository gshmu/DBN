package com.dci.intellij.dbn.data.ui.table.basic;

import com.dci.intellij.dbn.data.model.ColumnInfo;
import com.dci.intellij.dbn.data.model.DataModelHeader;
import com.intellij.ui.SpeedSearchBase;

public class BasicTableSpeedSearch extends SpeedSearchBase<BasicTable> {
    private BasicTable table;

    private ColumnInfo[] columnInfos;
    private int columnIndex = 0;

    public BasicTableSpeedSearch(BasicTable table) {
        super(table);
        this.table = table;
    }

    protected int getSelectedIndex() {
        return columnIndex;
    }

    protected Object[] getAllElements() {
        if (columnInfos == null) {
            DataModelHeader modelHeader = table.getModel().getHeader();
            columnInfos = modelHeader.getColumnInfos().toArray(new ColumnInfo[modelHeader.getColumnCount()]);
        }
        return columnInfos;
    }

    protected String getElementText(Object o) {
        ColumnInfo columnInfo = (ColumnInfo) o;
        return columnInfo.getName();
    }

    protected void selectElement(Object o, String s) {
        for(ColumnInfo columnInfo : columnInfos) {
            if (columnInfo == o) {
                columnIndex = columnInfo.getColumnIndex();
                int rowIndex = table.getSelectedRow();
                if (rowIndex == -1) rowIndex = 0;
                table.scrollRectToVisible(table.getCellRect(rowIndex, columnIndex, true));
                table.setColumnSelectionInterval(columnIndex, columnIndex);
                break;
            }
        }
    }

    
}