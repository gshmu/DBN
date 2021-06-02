package com.dci.intellij.dbn.common.ui.table;

import javax.swing.table.TableRowSorter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class DBNTableSorter<R, M extends DBNTableModel<R>> extends TableRowSorter<M> {
    private final Map<Integer, Comparator> comparators = new HashMap<>();

    public DBNTableSorter(M model) {
        super(model);
        setMaxSortKeys(1);
    }

    @Override
    public Comparator<?> getComparator(int column) {
        M model = getModel();
        return comparators.computeIfAbsent(column, col -> (Comparator<R>) (row1, row2) -> {
            Comparable value1 = model.getColumnValue(row1, col);
            Comparable value2 = model.getColumnValue(row2, col);
            return value1 == null ? -1 : value2 == null ? 1 : value1.compareTo(value2);
        });
    }

    @Override
    protected boolean useToString(int column) {
        return false;
    }


}
