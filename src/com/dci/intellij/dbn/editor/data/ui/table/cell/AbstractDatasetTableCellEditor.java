package com.dci.intellij.dbn.editor.data.ui.table.cell;

import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;
import java.text.ParseException;
import java.util.EventObject;

import javax.swing.*;
import javax.swing.table.TableCellEditor;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.locale.Formatter;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.data.editor.ui.DataEditorComponent;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.editor.data.model.DatasetEditorModelCell;
import com.dci.intellij.dbn.editor.data.model.DatasetEditorModelCellValueListener;
import com.dci.intellij.dbn.editor.data.options.DataEditorGeneralSettings;
import com.dci.intellij.dbn.editor.data.options.DataEditorSettings;
import com.dci.intellij.dbn.editor.data.ui.table.DatasetEditorTable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;

public abstract class AbstractDatasetTableCellEditor extends AbstractCellEditor implements TableCellEditor, Disposable {
    private DataEditorComponent editorComponent;
    private int clickCountToStart = 1;
    private DatasetEditorModelCell cell;
    protected DataEditorSettings settings;

    private DatasetEditorTable table;

    private DatasetEditorModelCellValueListener cellValueListener = cell -> {
        if (cell == AbstractDatasetTableCellEditor.this.cell) {
            ConditionalLaterInvocator.invoke(this::setCellValueToEditor);
        }
    };

    AbstractDatasetTableCellEditor(DatasetEditorTable table, DataEditorComponent editorComponent) {
        this.table = table;
        this.editorComponent = editorComponent;

        Project project = table.getProject();
        this.settings = DataEditorSettings.getInstance(project);

        this.clickCountToStart = 2;
        editorComponent.getTextField().addActionListener(new EditorDelegate());
        EventUtil.subscribe(project, this, DatasetEditorModelCellValueListener.TOPIC, cellValueListener);

        table.addPropertyChangeListener(evt -> {
            Object newValue = evt.getNewValue();
            if (newValue instanceof Font) {
                Font newFont = (Font) newValue;
                getEditorComponent().setFont(newFont);
            }
        });

        Disposer.register(this, editorComponent);
    }

    public DatasetEditorTable getTable() {
        return table;
    }



    public JComponent getEditorComponent() {
        return (JComponent) editorComponent;
    }

    public void setCell(DatasetEditorModelCell cell) {
        this.cell = cell;
    }

    public DatasetEditorModelCell getCell() {
        return cell;
    }

    public JTextField getTextField() {
        return editorComponent.getTextField();
    }

    public boolean isCellEditable(EventObject event) {
        if (event instanceof MouseEvent) {
            MouseEvent mouseEvent = (MouseEvent) event;
            return mouseEvent.getClickCount() >= clickCountToStart;
        }
        return true;
    }

    public boolean shouldSelectCell(EventObject event) {
        return true;
    }

    public boolean stopCellEditing() {
        fireEditingStopped();
        return true;
    }

    public void cancelCellEditing() {
        fireEditingCanceled();
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,  int column) {
        cell = (DatasetEditorModelCell) value;
        setCellValueToEditor();
        return (Component) editorComponent;
    }

    private void setCellValueToEditor() {
        if (cell != null) {
            Object userValue = cell.getUserValue();
            if (userValue instanceof String) {
                editorComponent.setText((String) userValue);
            } else {
                Formatter formatter = cell.getFormatter();
                String stringValue = formatter.formatObject(userValue);
                editorComponent.setText(stringValue);
            }
        } else {
            editorComponent.setText("");
        }
    }

    public Object getCellEditorValue() {
        DBDataType dataType = cell.getColumnInfo().getDataType();
        Class clazz = dataType.getTypeClass();
        try {
            String textValue = editorComponent.getText();
            
            
            boolean trim = true;
            if (clazz == String.class) {
                DataEditorGeneralSettings generalSettings = settings.getGeneralSettings();
                boolean isEmpty = StringUtil.isEmptyOrSpaces(textValue);
                trim = (isEmpty && generalSettings.getConvertEmptyStringsToNull().value()) ||
                       (!isEmpty && generalSettings.getTrimWhitespaces().value());
            }
            
            if (trim) textValue = textValue.trim();
            
            if (textValue.length() > 0) {
                Formatter formatter = cell.getFormatter();
                Object value = formatter.parseObject(clazz, textValue);
                return dataType.getNativeDataType().getDataTypeDefinition().convert(value);
            } else {
                return null;
            }
        } catch (ParseException e) {
            throw new IllegalArgumentException("Can not convert " + editorComponent.getText() + " to " + dataType.getName());
        }
    }

    public Object getCellEditorValueLenient() {
        return editorComponent.getText().trim();
    }

    /********************************************************
     *                    EditorDelegate                    *
     ********************************************************/
    protected class EditorDelegate implements ActionListener, ItemListener, Serializable {

        public void actionPerformed(ActionEvent e) {
            AbstractDatasetTableCellEditor.this.stopCellEditing();
        }

        public void itemStateChanged(ItemEvent e) {
            AbstractDatasetTableCellEditor.this.stopCellEditing();
        }
    }


    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    private boolean disposed;

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public void dispose() {
        if (!disposed) {
            disposed = true;
            editorComponent = null;
            settings = null;
            table = null;
            cell = null;
        }
    }
}
