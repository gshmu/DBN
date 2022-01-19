package com.dci.intellij.dbn.data.record.ui;

import com.dci.intellij.dbn.common.ui.MouseUtil;
import com.dci.intellij.dbn.common.util.TextAttributes;
import com.dci.intellij.dbn.data.grid.color.DataGridTextAttributesKeys;
import com.dci.intellij.dbn.data.record.DatasetRecord;
import com.dci.intellij.dbn.editor.data.DatasetEditorManager;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterInput;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBConstraint;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.Nullable;

import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

class ColumnValueTextField extends JTextField {
    private final DatasetRecord record;
    private final DBObjectRef<DBColumn> columnRef;

    ColumnValueTextField(DatasetRecord record, DBColumn column) {
        this.record = record;
        this.columnRef = DBObjectRef.of(column);
        if (column.isPrimaryKey()) {
            SimpleTextAttributes textAttributes = TextAttributes.getSimpleTextAttributes(DataGridTextAttributesKeys.PRIMARY_KEY);
            setForeground(textAttributes.getFgColor());
            Color background = textAttributes.getBgColor();
            if (background != null) {
                setBackground(background);
            }
        } else if (column.isForeignKey()) {
            addMouseListener(mouseListener);
            SimpleTextAttributes textAttributes = TextAttributes.getSimpleTextAttributes(DataGridTextAttributesKeys.FOREIGN_KEY);
            setForeground(textAttributes.getFgColor());
            Color background = textAttributes.getBgColor();
            if (background != null) {
                setBackground(background);
            }
        }

    }

    @Override
    protected void processMouseMotionEvent(MouseEvent e) {
        DBColumn column = getColumn();
        if (column != null && column.isForeignKey()) {
            if (e.isControlDown() && e.getID() != MouseEvent.MOUSE_DRAGGED && record.getColumnValue(column) != null) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                DBColumn foreignKeyColumn = column.getForeignKeyColumn();
                if (foreignKeyColumn != null) {
                    setToolTipText("<html>Show referenced <b>" + foreignKeyColumn.getDataset().getQualifiedName() + "</b> record<html>");
                }
            } else {
                super.processMouseMotionEvent(e);
                setCursor(Cursor.getDefaultCursor());
                setToolTipText(null);
            }
        } else {
            super.processMouseMotionEvent(e);
        }
    }
    
    @Nullable
    private DatasetFilterInput resolveForeignKeyRecord() {
        DBColumn column = getColumn();
        if (column != null) {
            for (DBConstraint constraint : column.getConstraints()) {
                if (constraint.isForeignKey()) {
                    DBConstraint foreignKeyConstraint = constraint.getForeignKeyConstraint();
                    if (foreignKeyConstraint != null) {
                        DBDataset foreignKeyDataset = foreignKeyConstraint.getDataset();
                        DatasetFilterInput filterInput = null;

                        for (DBColumn constraintColumn : constraint.getColumns()) {
                            DBColumn constraintCol = constraintColumn.getUndisposedEntity();
                            if (constraintCol != null) {
                                DBColumn foreignKeyColumn = constraintCol.getForeignKeyColumn();
                                if (foreignKeyColumn != null) {
                                    Object value = record.getColumnValue(column);
                                    if (filterInput == null) {
                                        filterInput = new DatasetFilterInput(foreignKeyDataset);
                                    }
                                    filterInput.setColumnValue(foreignKeyColumn, value);
                                }
                            }
                        }
                        return filterInput;
                    }
                }
            }
        }

        return null;
    }

    private DBColumn getColumn() {
        return DBObjectRef.get(columnRef);
    }

    private final MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent event) {
            DBColumn column = getColumn();
            if (column != null && MouseUtil.isNavigationEvent(event)) {
                if (column.isForeignKey() && record.getColumnValue(column) != null) {
                    DatasetFilterInput filterInput = resolveForeignKeyRecord();
                    DatasetEditorManager datasetEditorManager = DatasetEditorManager.getInstance(column.getProject());
                    datasetEditorManager.navigateToRecord(filterInput, event);
                    event.consume();
                }
            }
        }        
        
    };
}
