package com.dci.intellij.dbn.data.editor.ui;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.table.TableCellEditor;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.KeyUtil;
import com.dci.intellij.dbn.common.ui.list.EditableStringList;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.data.value.ArrayValue;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.DocumentAdapter;

public class ArrayEditorPopupProviderForm extends TextFieldPopupProviderForm {
    private JPanel mainPanel;
    private JPanel rightActionPanel;
    private JPanel leftActionPanel;
    private JScrollPane listScrollPane;

    private ArrayEditorList list;
    private boolean changed;

    public ArrayEditorPopupProviderForm(TextFieldWithPopup textField, boolean isAutoPopup) {
        super(textField, isAutoPopup);

        ActionToolbar leftActionToolbar = ActionUtil.createActionToolbar(
                "DBNavigator.Place.DataEditor.TextAreaPopup", true,
                new AddAction(),
                new RemoveAction());
        leftActionPanel.add(leftActionToolbar.getComponent(), BorderLayout.WEST);

        ActionToolbar rightActionToolbar = ActionUtil.createActionToolbar(
                "DBNavigator.Place.DataEditor.TextAreaPopup", true,
                new DeleteAction(),
                new RevertAction(),
                new AcceptAction());
        rightActionPanel.add(rightActionToolbar.getComponent(), BorderLayout.EAST);
        list = new ArrayEditorList();
        listScrollPane.setViewportView(list);
        listScrollPane.getViewport().setBackground(list.getBackground());
        list.initTableGutter();
        list.addKeyListener(this);
        mainPanel.addKeyListener(this);
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    private class ArrayEditorList extends EditableStringList {
        public ArrayEditorList() {
            super(false, true);
        }

        @Override
        public Component prepareEditor(TableCellEditor editor, int rowIndex, int columnIndex) {
            Component component = super.prepareEditor(editor, rowIndex, columnIndex);
            component.addKeyListener(ArrayEditorPopupProviderForm.this);
            return component;
        }
    }

    public JBPopup createPopup() {
        JTextField textField = getTextField();

        List<String> stringValues = new ArrayList<String>();
        try {
            Object userValue = getEditorComponent().getUserValueHolder().getUserValue();
            ArrayValue array = (ArrayValue) userValue;
            stringValues.addAll(array.read());
        } catch (SQLException e) {
            MessageUtil.showErrorDialog(e.getMessage(), e);
            return null;
        }
        list.setStringValues(stringValues);
        if (list.getModel().getSize() > 0) {
            list.selectCell(0,0);
        }

        //editorTextArea.setText(text);
        changed = false;
        //if (textField.isEditable()) editorTextArea.setCaretPosition(textField.getCaretPosition());
        //editorTextArea.setSelectionStart(textField.getSelectionStart());
        //editorTextArea.setSelectionEnd(textField.getSelectionEnd());
        //editorTextArea.getDocument().addDocumentListener(new DocumentListener());
        mainPanel.setPreferredSize(new Dimension(Math.max(200, textField.getWidth() + 32), 200));

        ComponentPopupBuilder popupBuilder = JBPopupFactory.getInstance().createComponentPopupBuilder(mainPanel, list);
        popupBuilder.setRequestFocus(true);
        popupBuilder.setResizable(true);
        return popupBuilder.createPopup();
    }

    public void handleKeyPressedEvent(KeyEvent e) {}
    public void handleKeyReleasedEvent(KeyEvent e) {}
    public void handleFocusLostEvent(FocusEvent e) {}

    public String getKeyShortcutName() {
        return IdeActions.ACTION_SHOW_INTENTION_ACTIONS;
    }

    public String getDescription() {
        return "Text Editor";
    }

    @Override
    public TextFieldPopupType getPopupType() {
        return TextFieldPopupType.TEXT_EDITOR;
    }

    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);
/*
        if (!e.isConsumed()) {
            if (matchesKeyEvent(e)) {
                editorTextArea.replaceSelection("\n");
            }
        }
*/
    }

    private class DocumentListener extends DocumentAdapter {
        protected void textChanged(DocumentEvent documentEvent) {
            changed = true;
        }
    }

    private class AcceptAction extends DumbAwareAction {
        private AcceptAction() {
            super("Accept changes", null, Icons.TEXT_CELL_EDIT_ACCEPT);
            setShortcutSet(KeyUtil.createShortcutSet(KeyEvent.VK_ENTER, InputEvent.ALT_MASK));
            registerAction(this);
        }

        public void actionPerformed(AnActionEvent e) {
            list.stopCellEditing();
            UserValueHolder userValueHolder = getEditorComponent().getUserValueHolder();
            userValueHolder.updateUserValue(list.getModel().getData(), false);

/*
            String text = editorTextArea.getText().trim();

            if (userValueHolder.getUserValue() instanceof String) {
                JTextField textField = getTextField();
                getEditorComponent().setEditable(text.indexOf('\n') == -1);

                textField.setText(text);
            }
*/
            hidePopup();
        }

        @Override
        public void update(AnActionEvent anActionEvent) {
            getTemplatePresentation().setEnabled(changed);
        }
    }

    private class RevertAction extends DumbAwareAction{
        private RevertAction() {
            super("Revert changes", null, Icons.TEXT_CELL_EDIT_REVERT);
            setShortcutSet(KeyUtil.createShortcutSet(KeyEvent.VK_ESCAPE, 0));
            registerAction(this);
        }

        public void actionPerformed(AnActionEvent e) {
            hidePopup();
        }

        @Override
        public void update(AnActionEvent anActionEvent) {
            getTemplatePresentation().setEnabled(changed);
        }
    }

    private class DeleteAction extends AnAction {
        private DeleteAction() {
            super("Delete content", null, Icons.TEXT_CELL_EDIT_DELETE);
            setShortcutSet(KeyUtil.createShortcutSet(KeyEvent.VK_DELETE, InputEvent.CTRL_MASK));
            registerAction(this);
        }

        public void actionPerformed(AnActionEvent e) {
            JTextField textField = getTextField();
            getEditorComponent().getUserValueHolder().updateUserValue(null, false);
            getEditorComponent().setEditable(true);
            textField.setText("");
            hidePopup();
        }
    }

    private class AddAction extends AnAction {
        private AddAction() {
            super("Add value", null, Icons.ARRAY_CELL_EDIT_ADD);
            setShortcutSet(KeyUtil.createShortcutSet(KeyEvent.VK_PLUS, InputEvent.CTRL_MASK));
            setShortcutSet(KeyUtil.createShortcutSet(KeyEvent.VK_ADD, InputEvent.CTRL_MASK));
            registerAction(this);
        }

        public void actionPerformed(AnActionEvent e) {
            list.insertRow();
        }
    }

    private class RemoveAction extends AnAction {
        private RemoveAction() {
            super("Remove value", null, Icons.ARRAY_CELL_EDIT_REMOVE);
            setShortcutSet(KeyUtil.createShortcutSet(KeyEvent.VK_MINUS, InputEvent.CTRL_MASK));
            setShortcutSet(KeyUtil.createShortcutSet(KeyEvent.VK_SUBTRACT, InputEvent.CTRL_MASK));
            registerAction(this);
        }

        public void actionPerformed(AnActionEvent e) {
            list.removeRow();
        }
    }

    public void dispose() {
        super.dispose();
    }

}
