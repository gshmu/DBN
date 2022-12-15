package com.dci.intellij.dbn.data.editor.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.color.Colors;
import com.dci.intellij.dbn.common.ui.list.EditableStringList;
import com.dci.intellij.dbn.common.ui.util.Borders;
import com.dci.intellij.dbn.common.ui.util.Keyboard;
import com.dci.intellij.dbn.common.ui.util.UserInterface;
import com.dci.intellij.dbn.common.util.Actions;
import com.dci.intellij.dbn.common.util.Messages;
import com.dci.intellij.dbn.common.util.TextAttributes;
import com.dci.intellij.dbn.data.grid.color.DataGridTextAttributesKeys;
import com.dci.intellij.dbn.data.value.ArrayValue;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ArrayEditorPopupProviderForm extends TextFieldPopupProviderForm {
    private JPanel mainPanel;
    private JPanel rightActionPanel;
    private JPanel leftActionPanel;
    private JScrollPane listScrollPane;

    private final ArrayEditorList list;
    private boolean changed;

    public ArrayEditorPopupProviderForm(TextFieldWithPopup<?> textField, boolean autoPopup) {
        super(textField, autoPopup, true);

        ActionToolbar leftActionToolbar = Actions.createActionToolbar(
                leftActionPanel,
                "DBNavigator.Place.DataEditor.TextAreaPopup", true,
                new AddAction(),
                new RemoveAction());
        leftActionPanel.add(leftActionToolbar.getComponent(), BorderLayout.WEST);

        ActionToolbar rightActionToolbar = Actions.createActionToolbar(
                rightActionPanel,
                "DBNavigator.Place.DataEditor.TextAreaPopup", true,
                new DeleteAction(),
                new RevertAction(),
                new AcceptAction());
        rightActionPanel.add(rightActionToolbar.getComponent(), BorderLayout.EAST);
        list = new ArrayEditorList();
        listScrollPane.setViewportView(list);
        listScrollPane.getViewport().setBackground(Colors.getListBackground());
        listScrollPane.setBorder(Borders.COMPONENT_LINE_BORDER);
        list.initTableGutter();
        list.addKeyListener(this);
        Color background = Colors.getEditorBackground();
        list.setBackground(background);
        listScrollPane.getViewport().setBackground(background);

        mainPanel.addKeyListener(this);

        updateComponentColors();
        Colors.subscribe(this, () -> updateComponentColors());
    }

    private void updateComponentColors() {
        UserInterface.changePanelBackground(mainPanel, Colors.getPanelBackground());

        SimpleTextAttributes textAttributes = TextAttributes.getSimpleTextAttributes(DataGridTextAttributesKeys.DEFAULT_PLAIN_DATA);
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return list;
    }

    private class ArrayEditorList extends EditableStringList {
        public ArrayEditorList() {
            super(ArrayEditorPopupProviderForm.this, false, true);
        }

        @Override
        public Component prepareEditor(TableCellEditor editor, int rowIndex, int columnIndex) {
            Component component = super.prepareEditor(editor, rowIndex, columnIndex);
            component.addKeyListener(ArrayEditorPopupProviderForm.this);
            return component;
        }
    }

    @Override
    public JBPopup createPopup() {
        JTextField textField = getTextField();

        List<String> stringValues = new ArrayList<>();
        UserValueHolder<Object> userValueHolder = getEditorComponent().getUserValueHolder();
        Project project = getProject();
        try {
            Object userValue = userValueHolder.getUserValue();
            ArrayValue array = (ArrayValue) userValue;
            List<String> values = array == null ? null : array.read();
            if (values != null) {
                stringValues.addAll(values);
            }
        } catch (SQLException e) {
            Messages.showErrorDialog(project, e.getMessage(), e);
            return null;
        }
        list.setStringValues(stringValues);
        if (list.getModel().getRowCount() > 0) {
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
        popupBuilder.setDimensionServiceKey(project, "ArrayEditor." + userValueHolder.getName(), false);
        return popupBuilder.createPopup();
    }

    @Override
    public void handleKeyPressedEvent(KeyEvent e) {}
    @Override
    public void handleKeyReleasedEvent(KeyEvent e) {}
    @Override
    public void handleFocusLostEvent(FocusEvent e) {}

    @Override
    public String getKeyShortcutName() {
        return IdeActions.ACTION_SHOW_INTENTION_ACTIONS;
    }

    @Override
    public String getDescription() {
        return "Array Editor";
    }

    @Override
    public TextFieldPopupType getPopupType() {
        return TextFieldPopupType.TEXT_EDITOR;
    }

    @Nullable
    @Override
    public Icon getButtonIcon() {
        return Icons.DATA_EDITOR_BROWSE;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!e.isConsumed()) {
            if (e.getKeyCode() == 27) {
                if (list.isEditing()) {
                    list.stopCellEditing();
                }
            } else {
                super.keyPressed(e);
            }
        }

    }

    private class DocumentListener extends DocumentAdapter {
        @Override
        protected void textChanged(@NotNull DocumentEvent e) {
            changed = true;
        }
    }

    private class AcceptAction extends DumbAwareAction {
        private AcceptAction() {
            super("Accept Changes", null, Icons.TEXT_CELL_EDIT_ACCEPT);
            setShortcutSet(Keyboard.createShortcutSet(KeyEvent.VK_ENTER, InputEvent.ALT_DOWN_MASK));
            registerAction(this);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
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
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(changed);
        }
    }

    private class RevertAction extends DumbAwareAction{
        private RevertAction() {
            super("Revert Changes", null, Icons.TEXT_CELL_EDIT_REVERT);
            setShortcutSet(Keyboard.createShortcutSet(KeyEvent.VK_ESCAPE, 0));
            registerAction(this);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            hidePopup();
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(changed);
        }
    }

    private class DeleteAction extends AnAction {
        private DeleteAction() {
            super("Delete Content", null, Icons.TEXT_CELL_EDIT_DELETE);
            setShortcutSet(Keyboard.createShortcutSet(KeyEvent.VK_DELETE, UserInterface.ctrlDownMask()));
            registerAction(this);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            TextFieldWithPopup editorComponent = getEditorComponent();
            editorComponent.getUserValueHolder().updateUserValue(new ArrayList<String>(), false);
            hidePopup();
        }
    }

    private class AddAction extends AnAction {
        private AddAction() {
            super("Add Value", null, Icons.ARRAY_CELL_EDIT_ADD);
            setShortcutSet(Keyboard.createShortcutSet(KeyEvent.VK_PLUS, UserInterface.ctrlDownMask()));
            setShortcutSet(Keyboard.createShortcutSet(KeyEvent.VK_ADD, UserInterface.ctrlDownMask()));
            registerAction(this);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            list.insertRow();
        }
    }

    private class RemoveAction extends AnAction {
        private RemoveAction() {
            super("Remove Value", null, Icons.ARRAY_CELL_EDIT_REMOVE);
            setShortcutSet(Keyboard.createShortcutSet(KeyEvent.VK_MINUS, UserInterface.ctrlDownMask()));
            setShortcutSet(Keyboard.createShortcutSet(KeyEvent.VK_SUBTRACT, UserInterface.ctrlDownMask()));
            registerAction(this);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            list.removeRow();
        }
    }
}
