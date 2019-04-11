package com.dci.intellij.dbn.data.model.sortable;

import com.dci.intellij.dbn.data.model.DataModelCell;
import com.dci.intellij.dbn.data.model.basic.BasicDataModelCell;
import org.jetbrains.annotations.NotNull;

public class SortableDataModelCell<
        R extends SortableDataModelRow<M, ? extends SortableDataModelCell<R, M>>,
        M extends SortableDataModel<R, ? extends SortableDataModelCell<R, M>>>
        extends BasicDataModelCell<R, M>
        implements Comparable {

    public SortableDataModelCell(R row, Object userValue, int index) {
        super(userValue, row, index);
    }

    @NotNull
    @Override
    public M getModel() {
        return super.getModel();
    }

    @NotNull
    @Override
    public R getRow() {
        return super.getRow();
    }

    @Override
    public int compareTo(@NotNull Object o) {
        DataModelCell cell = (DataModelCell) o;
        Comparable local = (Comparable) getUserValue();
        Comparable remote = (Comparable) cell.getUserValue();
        boolean nullsFirst = getModel().isSortingNullsFirst();

        if (local == null && remote == null) return 0;
        if (local == null) return nullsFirst ? -1 : 1;
        if (remote == null) return nullsFirst ? 1 : -1;
        // local class may differ from remote class for
        // columns with data conversion error
        if (local.getClass().equals(remote.getClass())) {
            return local.compareTo(remote);
        } else {
            Class typeClass = cell.getColumnInfo().getDataType().getTypeClass();
            return local.getClass().equals(typeClass) ? 1 :
                   remote.getClass().equals(typeClass) ? -1 : 0;
        }
    }
}
