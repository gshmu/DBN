package com.dci.intellij.dbn.data.model.basic;

import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.property.PropertyHolderImpl;
import com.dci.intellij.dbn.data.model.DataModelCell;
import com.dci.intellij.dbn.data.model.DataModelRow;
import com.dci.intellij.dbn.editor.data.model.RecordStatus;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BasicDataModelRow<T extends DataModelCell> extends PropertyHolderImpl<RecordStatus> implements DataModelRow<T> {
    protected BasicDataModel model;
    protected List<T> cells;
    private int index;

    public BasicDataModelRow(BasicDataModel model) {
        cells = new ArrayList<T>(model.getColumnCount());
        this.model = model;
    }

    @Override
    protected RecordStatus[] properties() {
        return RecordStatus.values();
    }

    protected void addCell(T cell) {
        getCells().add(cell);
    }

    @NotNull
    public BasicDataModel getModel() {
        return FailsafeUtil.get(model);
    }

    public List<T> getCells() {
        FailsafeUtil.ensure(this);
        return FailsafeUtil.get(cells);
    }


    @Override
    public final T getCell(String columnName) {
        List<T> cells = getCells();
        if (cells != null) {
            for (T cell : cells) {
                if (cell.getColumnInfo().getName().equals(columnName)) {
                    return cell;
                }
            }
        }
        return null;
    }

    @Override
    public final Object getCellValue(String columnName) {
        T cell = getCell(columnName);
        if (cell != null) {
            return cell.getUserValue();
        }
        return null;
    }



    public T getCellAtIndex(int index) {
        List<T> cells = getCells();
        return cells.size()> index ? cells.get(index) : null;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int indexOf(T cell) {
        return getCells().indexOf(cell);
    }

    public Project getProject() {
        return getModel().getProject();
    }

    /********************************************************
     *                    Disposable                        *
     ********************************************************/

    @Override
    public boolean isDisposed() {
        return is(RecordStatus.DISPOSED);
    }

    public void dispose() {
        if (!isDisposed()) {
            set(RecordStatus.DISPOSED, true);
            DisposerUtil.dispose(cells);
            cells = null;
            model = null;
        }
    }
}
