package com.dci.intellij.dbn.data.model.basic;

import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.dispose.Nullifiable;
import com.dci.intellij.dbn.common.property.DisposablePropertyHolder;
import com.dci.intellij.dbn.data.model.DataModelCell;
import com.dci.intellij.dbn.data.model.DataModelRow;
import com.dci.intellij.dbn.editor.data.model.RecordStatus;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Nullifiable
public class BasicDataModelRow<
        M extends BasicDataModel<? extends BasicDataModelRow<M, C>, C>,
        C extends DataModelCell<? extends BasicDataModelRow<M, C>, M>>
        extends DisposablePropertyHolder<RecordStatus>
        implements DataModelRow<M, C> {

    private WeakRef<M> model;
    private List<C> cells;
    private int index;

    public BasicDataModelRow(M model) {
        cells = new ArrayList<C>(model.getColumnCount());
        this.model = WeakRef.from(model);
    }

    @Override
    protected RecordStatus[] properties() {
        return RecordStatus.values();
    }

    protected void addCell(C cell) {
        cells.add(cell);
    }

    @Override
    @NotNull
    public M getModel() {
        return model.nn();
    }

    @Override
    public List<C> getCells() {
        return cells;
    }


    @Override
    public final C getCell(String columnName) {
        for (C cell : cells) {
            if (cell.getColumnInfo().getName().equals(columnName)) {
                return cell;
            }
        }
        return null;
    }

    @Override
    public final Object getCellValue(String columnName) {
        C cell = getCell(columnName);
        if (cell != null) {
            return cell.getUserValue();
        }
        return null;
    }



    @Nullable
    @Override
    public C getCellAtIndex(int index) {
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

    public int indexOf(C cell) {
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
