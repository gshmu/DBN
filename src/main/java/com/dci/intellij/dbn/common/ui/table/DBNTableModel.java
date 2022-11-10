package com.dci.intellij.dbn.common.ui.table;

import com.dci.intellij.dbn.common.dispose.StatefulDisposable;

import javax.swing.table.TableModel;

public interface DBNTableModel<R> extends TableModel, StatefulDisposable {
    default String getPresentableValue(R rowObject, int column) {
        return rowObject == null ? "" : rowObject.toString();
    };

    default Object getValue(R rowObject, int column) {
        throw new UnsupportedOperationException();
    };
}
