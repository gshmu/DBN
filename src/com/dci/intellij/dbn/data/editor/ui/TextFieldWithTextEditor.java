package com.dci.intellij.dbn.data.editor.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ProjectRef;
import com.dci.intellij.dbn.common.dispose.Nullifiable;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.ui.KeyUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.data.editor.text.TextEditorAdapter;
import com.dci.intellij.dbn.data.editor.text.ui.TextEditorDialog;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

@Nullifiable
public class TextFieldWithTextEditor extends JPanel implements DataEditorComponent, TextEditorAdapter {
    private JTextField textField;
    private JLabel button;

    private UserValueHolder userValueHolder;
    private ProjectRef projectRef;
    private String displayValue;

    public TextFieldWithTextEditor(@NotNull Project project) {
        this(project, null);
    }

    public TextFieldWithTextEditor(@NotNull Project project, String displayValue) {
        super(new BorderLayout(2, 0));
        this.projectRef = ProjectRef.from(project);
        this.displayValue = displayValue;
        setBounds(0, 0, 0, 0);

        textField = new JBTextField();
        textField.setMargin(JBUI.insets(1, 3, 1, 1));
        add(textField, BorderLayout.CENTER);

        button = new JLabel(Icons.DATA_EDITOR_BROWSE);
        button.setBorder(BUTTON_BORDER);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(mouseListener);
        Shortcut[] shortcuts = KeyUtil.getShortcuts(IdeActions.ACTION_SHOW_INTENTION_ACTIONS);
        String shortcutText = KeymapUtil.getShortcutsText(shortcuts);

        button.setToolTipText("Open editor (" + shortcutText + ')');
        add(button, BorderLayout.EAST);
        if (StringUtil.isNotEmpty(displayValue)) {
            textField.setText(displayValue);
            textField.setEnabled(false);
            textField.setDisabledTextColor(UIUtil.getLabelDisabledForeground());
        }
        textField.setPreferredSize(new Dimension(150, -1));
        textField.addKeyListener(keyListener);
        textField.setEditable(false);

        button.addKeyListener(keyListener);
        addKeyListener(keyListener);

        customizeButton(button);
        customizeTextField(textField);
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        if (textField != null) textField.setFont(font);
    }

    @Override
    public void setEnabled(boolean enabled) {
        textField.setEditable(enabled);
    }

    @Override
    public void setEditable(boolean editable){
        textField.setEditable(editable);
    }

    @Override
    public boolean isEditable() {
        return textField.isEditable();
    }

    @Override
    public void setUserValueHolder(UserValueHolder userValueHolder) {
        this.userValueHolder = userValueHolder;
    }


    public void customizeTextField(JTextField textField) {}
    public void customizeButton(JLabel button) {}

    public boolean isSelected() {
        Document document = textField.getDocument();
        return document.getLength() > 0 &&
               textField.getSelectionStart() == 0 &&
               textField.getSelectionEnd() == document.getLength();
    }

    public void clearSelection() {
        if (isSelected()) {
            textField.setSelectionStart(0);
            textField.setSelectionEnd(0);
            textField.setCaretPosition(0);
        }
    }

    @Override
    public JTextField getTextField() {
        return textField;
    }

    @Override
    public String getText() {
        return textField.getText();
    }

    @Override
    public void setText(String text) {
        textField.setText(text);
    }

    public JLabel getButton() {
        return button;
    }

    public void openEditor() {
        TextEditorDialog.show(getProject(), this);
    }

    @NotNull
    public Project getProject() {
        return projectRef.nn();
    }

    /********************************************************
     *                      KeyListener                     *
     ********************************************************/
    private KeyListener keyListener = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent keyEvent) {
            Shortcut[] shortcuts = KeyUtil.getShortcuts(IdeActions.ACTION_SHOW_INTENTION_ACTIONS);
            if (!keyEvent.isConsumed() && KeyUtil.match(shortcuts, keyEvent)) {
                keyEvent.consume();
                openEditor();
            }
        }
    };
    /********************************************************
     *                    ActionListener                    *
     ********************************************************/
    private ActionListener actionListener = e -> openEditor();

    private MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            openEditor();
        }
    };

    @Override
    public UserValueHolder getUserValueHolder() {
        return userValueHolder;
    }

    /********************************************************
     *                 TextEditorListener                   *
     ********************************************************/
    @Override
    public void afterUpdate() {
        Object userValue = userValueHolder.getUserValue();
        if (userValue instanceof String && StringUtil.isEmpty(displayValue)) {
            Dispatch.invoke(() -> {
                String text = (String) userValue;
                setEditable(text.length() < 1000 && text.indexOf('\n') == -1);
                setText(text);
            });
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
    public void markDisposed() {
        disposed = true;
    }
}
