package com.dci.intellij.dbn.editor.data.ui.table.cell;

import com.dci.intellij.dbn.common.color.Colors;
import com.dci.intellij.dbn.common.ui.misc.DBNButton;
import com.dci.intellij.dbn.common.ui.util.Borders;
import com.dci.intellij.dbn.common.ui.util.Keyboard;
import com.dci.intellij.dbn.data.editor.ui.TextFieldWithTextEditor;
import com.dci.intellij.dbn.data.model.ColumnInfo;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.editor.data.model.DatasetEditorModelCell;
import com.dci.intellij.dbn.editor.data.ui.table.DatasetEditorTable;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.ui.RoundedLineBorder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.KeyEvent;

public class DatasetTableCellEditorWithTextEditor extends DatasetTableCellEditor {
    private static final Border BUTTON_OUTSIDE_BORDER = JBUI.Borders.empty(1);
    private static final Border BUTTON_INSIDE_BORDER = JBUI.Borders.empty(0, 2);
    private static final RoundedLineBorder BUTTON_LINE_BORDER = new RoundedLineBorder(Colors.BUTTON_BORDER_COLOR, 4);
    private static final CompoundBorder BUTTON_BORDER = new CompoundBorder(BUTTON_OUTSIDE_BORDER, new CompoundBorder(BUTTON_LINE_BORDER, BUTTON_INSIDE_BORDER));


    public DatasetTableCellEditorWithTextEditor(DatasetEditorTable table) {
        super(table, createTextField(table));
        TextFieldWithTextEditor editorComponent = getEditorComponent();
        JTextField textField = editorComponent.getTextField();
        textField.setBorder(Borders.EMPTY_BORDER);
    }

    private static TextFieldWithTextEditor createTextField(DatasetEditorTable table) {
        return new TextFieldWithTextEditor(table.getProject()) {
            @Override
            public void setEditable(boolean editable) {
                super.setEditable(editable);
                Color background = getTextField().getBackground();
                setBackground(background);
                getButton().setBackground(background);
            }

            @Override
            public void customizeButton(DBNButton button) {
                button.setBorder(Borders.insetBorder(1));
                button.setBackground(Colors.getTableBackground());
                int rowHeight = table.getRowHeight();
                button.setPreferredSize(new Dimension(Math.max(20, rowHeight), rowHeight - 2));
                button.getParent().setBackground(getTextField().getBackground());
                table.addPropertyChangeListener(e -> {
                    Object newProperty = e.getNewValue();
                    if (newProperty instanceof Font) {
                        int rowHeight1 = table.getRowHeight();
                        button.setPreferredSize(new Dimension(Math.max(20, rowHeight1), table.getRowHeight() - 2));
                    }
                });
            }
        };
    }

    @Override
    @NotNull
    public TextFieldWithTextEditor getEditorComponent() {
        return (TextFieldWithTextEditor) super.getEditorComponent();
    }

    @Override
    public void prepareEditor(@NotNull DatasetEditorModelCell cell) {
        getEditorComponent().setUserValueHolder(cell);
        setCell(cell);
        ColumnInfo columnInfo = cell.getColumnInfo();
        DBDataType dataType = columnInfo.getDataType();
        if (!dataType.isNative()) return;

        JTextField textField = getTextField();
        highlight(cell.hasError() ? HIGHLIGHT_TYPE_ERROR : HIGHLIGHT_TYPE_NONE);
        if (dataType.getNativeType().isLargeObject()) {
            setEditable(false);
        } else {
            Object object = cell.getUserValue();
            String userValue = object == null ? null :
                    object instanceof String ? (String) object
                    : object.toString();
            setEditable(userValue == null || (userValue.length() < 1000 && userValue.indexOf('\n') == -1));
        }
        selectText(textField);
    }

    @Override
    public void setEditable(boolean editable) {
        TextFieldWithTextEditor editorComponent = getEditorComponent();
        editorComponent.setEditable(editable);
    }

    /********************************************************
     *                      KeyListener                     *
     ********************************************************/
    @Override
    public void keyPressed(KeyEvent keyEvent) {
        Shortcut[] shortcuts = Keyboard.getShortcuts(IdeActions.ACTION_SHOW_INTENTION_ACTIONS);
        if (!keyEvent.isConsumed() && Keyboard.match(shortcuts, keyEvent)) {
            keyEvent.consume();
            getEditorComponent().openEditor();
        } else {
            super.keyPressed(keyEvent);
        }
    }

}
