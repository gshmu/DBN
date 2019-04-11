package com.dci.intellij.dbn.data.model.basic;

import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.Nullifiable;
import com.dci.intellij.dbn.common.property.DisposablePropertyHolder;
import com.dci.intellij.dbn.data.model.DataModelCell;
import com.dci.intellij.dbn.data.model.DataModelRow;
import com.dci.intellij.dbn.editor.data.model.RecordStatus;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Nullifiable
public class BasicDataModelRow<T extends DataModelCell> extends DisposablePropertyHolder<RecordStatus> implements DataModelRow<T> {
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
        cells.add(cell);
    }

    @Override
    @NotNull
    public BasicDataModel getModel() {
        return Failsafe.nn(model);
    }

    @Override
    public List<T> getCells() {
        return cells;
    }


    @Override
    public final T getCell(String columnName) {
        for (T cell : cells) {
            if (cell.getColumnInfo().getName().equals(columnName)) {
                return cell;
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



    @Nullable
    @Override
    public T getCellAtIndex(int index) {
        return cells.size() > index ? cells.get(index) : null;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }

    public int indexOf(T cell) {
        return cells.indexOf(cell);
    }

    public Project getProject() {
        return getModel().getProject();
    }

    @Override
    public void disposeInner() {
        Disposer.dispose(cells);
        super.disposeInner();
    }
}
