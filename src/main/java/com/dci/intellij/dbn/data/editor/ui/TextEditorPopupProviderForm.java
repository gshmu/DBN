package com.dci.intellij.dbn.data.editor.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.color.Colors;
import com.dci.intellij.dbn.common.ui.misc.DBNScrollPane;
import com.dci.intellij.dbn.common.ui.util.Borders;
import com.dci.intellij.dbn.common.ui.util.Keyboard;
import com.dci.intellij.dbn.common.ui.util.UserInterface;
import com.dci.intellij.dbn.common.util.Actions;
import com.dci.intellij.dbn.common.util.Commons;
import com.dci.intellij.dbn.common.util.Messages;
import com.dci.intellij.dbn.common.util.TextAttributes;
import com.dci.intellij.dbn.data.grid.color.DataGridTextAttributesKeys;
import com.dci.intellij.dbn.data.value.LargeObjectValue;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.sql.SQLException;

import static com.dci.intellij.dbn.common.ui.util.TextFields.onTextChange;
import static com.dci.intellij.dbn.diagnostics.Diagnostics.conditionallyLog;

public class TextEditorPopupProviderForm extends TextFieldPopupProviderForm {
    private JPanel mainPanel;
    private JPanel rightActionPanel;
    private JPanel leftActionPanel;
    private JTextArea editorTextArea;
    private DBNScrollPane textEditorScrollPane;

    private boolean changed;

    TextEditorPopupProviderForm(TextFieldWithPopup<?> textField, boolean autoPopup) {
        super(textField, autoPopup, true);
        editorTextArea.setBorder(JBUI.Borders.empty(4));
        editorTextArea.addKeyListener(this);
        editorTextArea.setWrapStyleWord(true);


        textEditorScrollPane.setBorder(Borders.COMPONENT_OUTLINE_BORDER);

        ActionToolbar leftActionToolbar = Actions.createActionToolbar(
                leftActionPanel,
                "DBNavigator.Place.DataEditor.TextAreaPopup", true);
        leftActionPanel.add(leftActionToolbar.getComponent(), BorderLayout.WEST);

        ActionToolbar rightActionToolbar = Actions.createActionToolbar(
                rightActionPanel,
                "DBNavigator.Place.DataEditor.TextAreaPopup", true,
                new DeleteAction(),
                new RevertAction(),
                new AcceptAction());
        rightActionPanel.add(rightActionToolbar.getComponent(), BorderLayout.EAST);

        updateComponentColors();
        Colors.subscribe(this, () -> updateComponentColors());
    }

    private void updateComponentColors() {
        UserInterface.changePanelBackground(mainPanel, Colors.getPanelBackground());

        SimpleTextAttributes textAttributes = TextAttributes.getSimpleTextAttributes(DataGridTextAttributesKeys.DEFAULT_PLAIN_DATA);
        editorTextArea.setBackground(Commons.nvl(
                textAttributes.getBgColor(),
                Colors.getTextFieldBackground()));

        editorTextArea.setForeground(Commons.nvl(
                textAttributes.getFgColor(),
                Colors.getTextFieldForeground()));
    }


    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return editorTextArea;
    }

    @Override
    public JBPopup createPopup() {
        JTextField textField = getTextField();
        String text = "";
        UserValueHolder userValueHolder = getEditorComponent().getUserValueHolder();
        if (textField.isEditable()) {
            text = textField.getText();
        } else {
            Object userValue = userValueHolder.getUserValue();
            if (userValue instanceof String) {
                text = (String) userValue;
            } else if (userValue instanceof LargeObjectValue) {
                LargeObjectValue largeObjectValue = (LargeObjectValue) userValue;
                try {
                    text = Commons.nvl(largeObjectValue.read(), "");
                } catch (SQLException e) {
                    conditionallyLog(e);
                    Messages.showErrorDialog(getProject(), e.getMessage(), e);
                    return null;
                }
            }
        }

        editorTextArea.setText(text);
        changed = false;
        if (textField.isEditable()) editorTextArea.setCaretPosition(textField.getCaretPosition());
        editorTextArea.setSelectionStart(textField.getSelectionStart());
        editorTextArea.setSelectionEnd(textField.getSelectionEnd());
        onTextChange(editorTextArea, e -> changed = true);
        mainPanel.setPreferredSize(new Dimension(Math.max(200, textField.getWidth() + 32), 160));

        ComponentPopupBuilder popupBuilder = JBPopupFactory.getInstance().createComponentPopupBuilder(mainPanel, editorTextArea);
        popupBuilder.setRequestFocus(true);
        popupBuilder.setResizable(true);
        popupBuilder.setDimensionServiceKey(getProject(), "TextEditor." + userValueHolder.getName(), false);
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
        return "Text Editor";
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
        super.keyPressed(e);
        if (!e.isConsumed()) {
            if (Keyboard.match(getShortcuts(), e)) {
                editorTextArea.replaceSelection("\n");
            }
        }
    }

    private class AcceptAction extends DumbAwareAction {
        private AcceptAction() {
            super("Accept changes", null, Icons.TEXT_CELL_EDIT_ACCEPT);
            setShortcutSet(Keyboard.createShortcutSet(KeyEvent.VK_ENTER, InputEvent.ALT_DOWN_MASK));
            registerAction(this);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            String text = editorTextArea.getText().trim();
            UserValueHolder userValueHolder = getEditorComponent().getUserValueHolder();
            userValueHolder.updateUserValue(text, false);

            if (userValueHolder.getUserValue() instanceof String) {
                JTextField textField = getTextField();
                getEditorComponent().setEditable(text.indexOf('\n') == -1);

                textField.setText(text);
            }
            hidePopup();
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(changed);
        }
    }

    private class RevertAction extends DumbAwareAction{
        private RevertAction() {
            super("Revert changes", null, Icons.TEXT_CELL_EDIT_REVERT);
            setShortcutSet(Keyboard.createShortcutSet(KeyEvent.VK_ESCAPE, 0));
            //registerAction(this);
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
            //registerAction(this);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            JTextField textField = getTextField();
            TextFieldWithPopup editorComponent = getEditorComponent();
            editorComponent.getUserValueHolder().updateUserValue(null, false);
            editorComponent.setEditable(true);
            textField.setText("");
            hidePopup();
        }
    }
}
