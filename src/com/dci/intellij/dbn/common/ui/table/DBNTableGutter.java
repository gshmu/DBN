package com.dci.intellij.dbn.common.ui.table;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.colors.EditorColorsListener;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class DBNTableGutter<T extends DBNTableWithGutter> extends JList implements Disposable, EditorColorsListener {
    private boolean disposed;
    private T table;

    public DBNTableGutter(T table) {
        super(table.getModel().getListModel());
        this.table = table;
        int rowHeight = table.getRowHeight();
        if (rowHeight != 0) setFixedCellHeight(rowHeight);
        setBackground(UIUtil.getPanelBackground());

        setCellRenderer(createCellRenderer());
        ApplicationManager.getApplication().getMessageBus().connect().subscribe(EditorColorsManager.TOPIC, this);
    }

    protected abstract ListCellRenderer createCellRenderer();

    @Override
    public void globalSchemeChange(@Nullable EditorColorsScheme scheme) {
        setCellRenderer(createCellRenderer());
    }

    @Override
    public ListModel getModel() {
        ListModel cachedModel = super.getModel();
        if (table == null) {
            return cachedModel;
        } else {
            ListModel listModel = table.getModel().getListModel();
            if (listModel != null && listModel != cachedModel) {
                setModel(listModel);
            }
            return listModel;
        }
    }

    public T getTable() {
        return table;
    }




    @Override
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public void markDisposed() {
        disposed = true;
    }

    @Override
    public void disposeInner() {
        table = null;
    }
}
