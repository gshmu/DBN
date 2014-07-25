package com.dci.intellij.dbn.editor.data.ui.table;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTableGutter;
import com.dci.intellij.dbn.editor.data.ui.table.renderer.DatasetEditorTableGutterRenderer;

public class DatasetEditorTableGutter extends BasicTableGutter {
    public DatasetEditorTableGutter(DatasetEditorTable table) {
        super(table);
        addMouseListener(mouseListener);
    }

    @Override
    protected ListCellRenderer createCellRenderer() {
        return new DatasetEditorTableGutterRenderer();
    }

    MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                getTable().getDatasetEditor().openRecordEditor(getSelectedIndex());
            }
        }
    };

    @Override
    public DatasetEditorTable getTable() {
        return (DatasetEditorTable) super.getTable();
    }

    @Override
    public void dispose() {
        super.dispose();
        removeMouseListener(mouseListener);
        mouseListener = null;
    }
}
