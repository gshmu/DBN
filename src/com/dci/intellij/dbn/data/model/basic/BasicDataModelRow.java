package com.dci.intellij.dbn.data.model.basic;

import java.util.ArrayList;
import java.util.List;

import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.data.model.DataModelCell;
import com.dci.intellij.dbn.data.model.DataModelRow;
import com.intellij.openapi.project.Project;

public class BasicDataModelRow<T extends DataModelCell> implements DataModelRow<T> {
    protected BasicDataModel model;
    protected List<T> cells;
    private int index;
    private boolean isDisposed;

    public BasicDataModelRow(BasicDataModel model) {
        cells = new ArrayList<T>(model.getColumnCount());
        this.model = model;
    }

    protected void addCell(T cell) {
        cells.add(cell);
    }

    public BasicDataModel getModel() {
        return model;
    }

    public List<T> getCells() {
        return cells;
    }

    public T getCellAtIndex(int index) {
        return cells.get(index);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int indexOf(T cell) {
        return cells.indexOf(cell);
    }


    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    private boolean disposed;

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    public void dispose() {
        if (!isDisposed) {
            isDisposed = true;
            DisposerUtil.dispose(cells);
            cells = null;
            model = null;
        }
    }

    public Project getProject() {
        return model == null ? null : model.getProject();
    }
}
