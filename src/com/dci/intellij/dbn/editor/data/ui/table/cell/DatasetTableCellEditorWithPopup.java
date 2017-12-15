package com.dci.intellij.dbn.editor.data.ui.table.cell;

import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.data.editor.ui.TextFieldPopupProvider;
import com.dci.intellij.dbn.data.editor.ui.TextFieldWithPopup;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.editor.data.model.DatasetEditorModelCell;
import com.dci.intellij.dbn.editor.data.options.DataEditorPopupSettings;
import com.dci.intellij.dbn.editor.data.ui.table.DatasetEditorTable;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class DatasetTableCellEditorWithPopup extends DatasetTableCellEditor {
    public DatasetTableCellEditorWithPopup(DatasetEditorTable table) {
        super(table, new CustomTextFieldWithPopup(table));
    }

    public TextFieldWithPopup getEditorComponent() {
        return (TextFieldWithPopup) super.getEditorComponent();
    }

    @Override
    public void prepareEditor(final DatasetEditorModelCell cell) {
        getEditorComponent().setUserValueHolder(cell);
        super.prepareEditor(cell);

        // show automatic popup
        final TextFieldPopupProvider popupProvider = getEditorComponent().getAutoPopupProvider();
        if (popupProvider != null && showAutoPopup()) {
            Thread popupThread = new Thread() {
                   public void run() {
                       try {
                           sleep(settings.getPopupSettings().getDelay());
                       } catch (InterruptedException e) {
                           e.printStackTrace();
                       }

                       if (!cell.isDisposed() && cell.isEditing()) {
                           popupProvider.showPopup();
                       }
                   }
               };
               popupThread.start();
        }
    }

    @Override
    public void setEditable(boolean editable) {
        getEditorComponent().setEditable(editable);
    }


    private boolean showAutoPopup() {
        DataEditorPopupSettings settings = this.settings.getPopupSettings();
        DBDataType dataType = getCell().getColumnInfo().getDataType();
        if (dataType != null) {
            long dataLength = dataType.getLength();
            if (!isEditable()) return true;
            if (settings.isActive() && (settings.getDataLengthThreshold() < dataLength || dataLength == 0)) {
                if (settings.isActiveIfEmpty() || getTextField().getText().length() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void fireEditingCanceled() {
        getEditorComponent().hideActivePopup();
        super.fireEditingCanceled();
    }

    @Override
    protected void fireEditingStopped() {
        getEditorComponent().hideActivePopup();
        super.fireEditingStopped();
    }

    /********************************************************
     *                      KeyListener                     *
     ********************************************************/
    public void keyPressed(KeyEvent keyEvent) {
        if (!keyEvent.isConsumed()) {
            TextFieldPopupProvider popupProviderForm = getEditorComponent().getActivePopupProvider();
            if (popupProviderForm != null) {
                popupProviderForm.handleKeyPressedEvent(keyEvent);

            } else {
                popupProviderForm = getEditorComponent().getPopupProvider(keyEvent);
                if (popupProviderForm != null) {
                    getEditorComponent().hideActivePopup();
                    popupProviderForm.showPopup();
                } else {
                    super.keyPressed(keyEvent);
                }
            }
        }
    }

    public void keyReleased(KeyEvent keyEvent) {
        TextFieldPopupProvider popupProviderForm = getEditorComponent().getActivePopupProvider();
        if (popupProviderForm != null) {
            popupProviderForm.handleKeyReleasedEvent(keyEvent);

        }
    }

    /********************************************************
     *                  TextFieldWithPopup                  *
     ********************************************************/

    private static class CustomTextFieldWithPopup extends TextFieldWithPopup {
        protected static final EmptyBorder BUTTON_INSIDE_BORDER = JBUI.Borders.empty(0, 2);
        protected static final CompoundBorder BUTTON_BORDER = new CompoundBorder(BUTTON_OUTSIDE_BORDER, new CompoundBorder(BUTTON_LINE_BORDER, BUTTON_INSIDE_BORDER));

        private DatasetEditorTable table;

        private CustomTextFieldWithPopup(DatasetEditorTable table) {
            super(table.getProject());
            this.table = table;
            setBackground(table.getBackground());
        }

        @Override
        public void customizeTextField(JTextField textField) {
            textField.setBorder(Borders.EMPTY_BORDER);
            //textField.setBorder(new CompoundBorder(new LineBorder(Color.BLACK), new EmptyBorder(new Insets(1, 1, 1, 1))));
        }

        @Override
        public void customizeButton(final JLabel button) {
            button.setBorder(BUTTON_BORDER);
            button.setBackground(UIUtil.getTableBackground());
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            int rowHeight = table.getRowHeight();
            button.setPreferredSize(new Dimension(Math.max(20, rowHeight), rowHeight - 2));
            button.getParent().setBackground(button.getBackground());
            table.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent e) {
                    Object newProperty = e.getNewValue();
                    if (newProperty instanceof Font) {
                        int rowHeight = table.getRowHeight();
                        button.setPreferredSize(new Dimension(Math.max(20, rowHeight), table.getRowHeight() - 2));
                    }
                }
            });
        }

            @Override
        public void setEditable(boolean editable) {
            super.setEditable(editable);
            setBackground(getTextField().getBackground());
        }
    }
}
