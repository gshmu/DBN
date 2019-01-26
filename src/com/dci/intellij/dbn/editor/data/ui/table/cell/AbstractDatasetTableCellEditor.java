package com.dci.intellij.dbn.editor.data.ui.table.cell;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
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
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.text.ParseException;
import java.util.EventObject;

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



    @NotNull
    public DataEditorComponent getEditorComponent() {
        return FailsafeUtil.get(editorComponent);
    }

    public void setCell(DatasetEditorModelCell cell) {
        this.cell = cell;
    }

    public DatasetEditorModelCell getCell() {
        return cell;
    }

    public JTextField getTextField() {
        return getEditorComponent().getTextField();
    }

    @Override
    public boolean isCellEditable(EventObject event) {
        if (event instanceof MouseEvent) {
            MouseEvent mouseEvent = (MouseEvent) event;
            return mouseEvent.getClickCount() >= clickCountToStart;
        }
        return true;
    }

    @Override
    public boolean shouldSelectCell(EventObject event) {
        return true;
    }

    @Override
    public boolean stopCellEditing() {
        fireEditingStopped();
        return true;
    }

    @Override
    public void cancelCellEditing() {
        fireEditingCanceled();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        cell = (DatasetEditorModelCell) value;
        setCellValueToEditor();
        return (Component) getEditorComponent();
    }

    private void setCellValueToEditor() {
        DataEditorComponent editorComponent = getEditorComponent();

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

    @Override
    public Object getCellEditorValue() {
        DBDataType dataType = cell.getColumnInfo().getDataType();
        Class clazz = dataType.getTypeClass();
        try {
            String textValue = getEditorComponent().getText();
            
            
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
            throw new IllegalArgumentException("Can not convert " + getEditorComponent().getText() + " to " + dataType.getName());
        }
    }

    public Object getCellEditorValueLenient() {
        return getEditorComponent().getText().trim();
    }

    public boolean isEnabled() {
        return getEditorComponent().isEnabled();
    }

    public void setEnabled(boolean enabled) {
        getEditorComponent().setEnabled(enabled);
    }

    /********************************************************
     *                    EditorDelegate                    *
     ********************************************************/
    protected class EditorDelegate implements ActionListener, ItemListener, Serializable {

        @Override
        public void actionPerformed(ActionEvent e) {
            AbstractDatasetTableCellEditor.this.stopCellEditing();
        }

        @Override
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
