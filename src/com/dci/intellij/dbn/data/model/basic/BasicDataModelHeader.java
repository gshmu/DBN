package com.dci.intellij.dbn.data.model.basic;


import java.util.ArrayList;
import java.util.List;

import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.data.model.ColumnInfo;
import com.dci.intellij.dbn.data.model.DataModelHeader;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.intellij.openapi.util.Disposer;

public class BasicDataModelHeader<T extends ColumnInfo> extends DisposableBase implements DataModelHeader<T> {
    private List<T> columnInfos = new ArrayList<T>();


    protected void addColumnInfo(T columnInfo) {
        columnInfos.add(columnInfo);
        Disposer.register(this, columnInfo);
    }

    public List<T> getColumnInfos() {
        return columnInfos;
    }

    public T getColumnInfo(int columnIndex) {
        return columnInfos.get(columnIndex);
    }

    public int getColumnIndex(String name) {
        for (int i=0; i<columnInfos.size(); i++) {
            T columnInfo = columnInfos.get(i);
            if (columnInfo.getName().equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }

    public String getColumnName(int columnIndex) {
        return getColumnInfo(columnIndex).getName();
    }

    public DBDataType getColumnDataType(int columnIndex) {
        return getColumnInfo(columnIndex).getDataType();
    }

    public int getColumnCount() {
        return columnInfos.size();
    }


    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    @Override
    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
            columnInfos.clear();
        }
    }
}
