package com.dci.intellij.dbn.data.editor.ui;

import com.dci.intellij.dbn.common.Colors;
import com.dci.intellij.dbn.common.dispose.DisposableContainer;
import com.dci.intellij.dbn.common.project.ProjectRef;
import com.dci.intellij.dbn.common.ui.KeyUtil;
import com.dci.intellij.dbn.common.ui.panel.DBNPanelImpl;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.JBUI;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class TextFieldWithPopup<T extends JComponent> extends DBNPanelImpl implements DataEditorComponent {
    private final JTextField textField;
    private final JPanel buttonsPanel;

    private @Getter @Setter UserValueHolder<?> userValueHolder;
    private final List<TextFieldPopupProvider> popupProviders = DisposableContainer.list(this);
    private final ProjectRef project;
    private T parentComponent;

    public TextFieldWithPopup(Project project) {
        this(project, null);

    }
    public TextFieldWithPopup(Project project, @Nullable T parentComponent) {
        setLayout(new BorderLayout());
        this.project = ProjectRef.of(project);
        this.parentComponent = parentComponent;

        textField = new JTextField();
        textField.setMargin(JBUI.insets(0, 1));

        Dimension textFieldPreferredSize = textField.getPreferredSize();
        Dimension maximumSize = new Dimension((int) textFieldPreferredSize.getWidth(), (int) textFieldPreferredSize.getHeight());

        textField.setMaximumSize(maximumSize);
        add(textField, BorderLayout.CENTER);

        buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        buttonsPanel.setMaximumSize(maximumSize);
        add(buttonsPanel, BorderLayout.EAST);

        textField.addKeyListener(keyListener);
        textField.addFocusListener(focusListener);

        customizeTextField(textField);
    }

    @Override
    public void setBackground(Color color) {
        super.setBackground(color);
        if (textField != null) textField.setBackground(color);
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        if (textField != null) textField.setFont(font);
    }

    @Override
    public void setBorder(Border border) {
        super.setBorder(border);
    }

    @NotNull
    public Project getProject() {
        return project.ensure();
    }

    @Nullable
    public T getParentComponent() {
        return parentComponent;
    }

    @Override
    public void setEditable(boolean editable){
        textField.setEditable(editable);
    }

    @Override
    public boolean isEditable() {
        return textField.isEditable();
    }
                                                                                  
    public void customizeTextField(JTextField textField) {}

    public void customizeButton(JLabel button) {
        int width = (int) button.getPreferredSize().getWidth();
        int height = (int) textField.getPreferredSize().getHeight();
        button.setPreferredSize(new Dimension(width, height));
        button.setMaximumSize(new Dimension(width, height));
    }

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

    @Override
    public void setEnabled(boolean enabled) {
        //textField.setEnabled(enabled);
        textField.setEditable(enabled);
        for (TextFieldPopupProvider popupProvider : popupProviders) {
            JLabel button = popupProvider.getButton();
            if (button != null) {
                button.setVisible(enabled);
            }
        }
    }

    /******************************************************
     *                    PopupProviders                  *
     ******************************************************/
    public void createValuesListPopup(ListPopupValuesProvider valuesProvider, boolean buttonVisible) {
        ValueListPopupProvider popupProvider = new ValueListPopupProvider(this, valuesProvider, false, buttonVisible);
        addPopupProvider(popupProvider);
    }

    public void createTextEditorPopup(boolean autoPopup) {
        TextEditorPopupProviderForm popupProvider = new TextEditorPopupProviderForm(this, autoPopup);
        addPopupProvider(popupProvider);
    }

    public void createCalendarPopup(boolean autoPopup) {
        CalendarPopupProviderForm popupProviderForm = new CalendarPopupProviderForm(this, autoPopup);
        addPopupProvider(popupProviderForm);
    }

    public void createArrayEditorPopup(boolean autoPopup) {
        ArrayEditorPopupProviderForm popupProviderForm = new ArrayEditorPopupProviderForm(this, autoPopup);
        addPopupProvider(popupProviderForm);
    }

    private void addPopupProvider(TextFieldPopupProvider popupProvider) {
        popupProviders.add(popupProvider);

        if (popupProvider.isButtonVisible()) {
            Icon buttonIcon = popupProvider.getButtonIcon();
            JLabel button = new JLabel(buttonIcon);
            button.setBorder(BUTTON_BORDER);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            String toolTipText = "Open " + popupProvider.getDescription();
            String keyShortcutDescription = popupProvider.getKeyShortcutDescription();
            if (keyShortcutDescription != null) {
                toolTipText += " (" + keyShortcutDescription + ')';
            }
            button.setToolTipText(toolTipText);

            button.addMouseListener(new ButtonMouseListener(popupProvider));
            buttonsPanel.add(button, buttonsPanel.getComponentCount());
            customizeButton(button);
            popupProvider.setButton(button);
            Colors.subscribe(this, () -> customizeButton(button));
        }
    }

    public void setPopupEnabled(TextFieldPopupType popupType, boolean enabled) {
        for (TextFieldPopupProvider popupProvider : popupProviders) {
            if (popupProvider.getPopupType() == popupType) {
                popupProvider.setEnabled(enabled);
                JLabel button = popupProvider.getButton();
                if (button != null) {
                    button.setVisible(enabled);
                }
                break;
            }
        }
    }

    public void hideActivePopup() {
        TextFieldPopupProvider popupProvider = getActivePopupProvider();
        if ( popupProvider != null) {
             popupProvider.hidePopup();
        }
    }

    public TextFieldPopupProvider getAutoPopupProvider() {
        for (TextFieldPopupProvider popupProvider : popupProviders) {
            if (popupProvider.isAutoPopup()) {
                return popupProvider;
            }
        }
        return null;
    }

    private TextFieldPopupProvider getDefaultPopupProvider() {
        return popupProviders.get(0);
    }

    public TextFieldPopupProvider getActivePopupProvider() {
        for (TextFieldPopupProvider popupProvider : popupProviders) {
            if (popupProvider.isShowingPopup()) {
                return popupProvider;
            }
        }
        return null;
    }

    public TextFieldPopupProvider getPopupProvider(KeyEvent keyEvent) {
        for (TextFieldPopupProvider popupProvider : popupProviders) {
            Shortcut[] shortcuts = popupProvider.getShortcuts();
            if (KeyUtil.match(shortcuts, keyEvent)) {
                return popupProvider;
            }
        }
        return null;
    }

    /********************************************************
     *                    FocusListener                     *
     ********************************************************/
    private final FocusListener focusListener = new FocusAdapter() {
        @Override
        public void focusLost(FocusEvent focusEvent) {
            TextFieldPopupProvider popupProvider = getActivePopupProvider();
            if (popupProvider != null) {
                popupProvider.handleFocusLostEvent(focusEvent);
            }
        }
    };

    /********************************************************
     *                      KeyListener                     *
     ********************************************************/
    private final KeyListener keyListener = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent keyEvent) {
            TextFieldPopupProvider popupProvider = getActivePopupProvider();
            if (popupProvider != null) {
                popupProvider.handleKeyPressedEvent(keyEvent);

            } else {
                popupProvider = getPopupProvider(keyEvent);
                if (popupProvider != null && popupProvider.isEnabled()) {
                    hideActivePopup();
                    popupProvider.showPopup();
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent keyEvent) {
            TextFieldPopupProvider popupProvider = getActivePopupProvider();
            if (popupProvider != null) {
                popupProvider.handleKeyReleasedEvent(keyEvent);

            }
        }
    };
    /********************************************************
     *                    ActionListener                    *
     ********************************************************/
    private final ActionListener actionListener = e -> {
        TextFieldPopupProvider defaultPopupProvider = getDefaultPopupProvider();
        TextFieldPopupProvider popupProvider = getActivePopupProvider();
        if (popupProvider == null || popupProvider != defaultPopupProvider) {
            hideActivePopup();
            defaultPopupProvider.showPopup();
        }
    };

    private class ButtonMouseListener extends MouseAdapter {
        TextFieldPopupProvider popupProvider;

        ButtonMouseListener(TextFieldPopupProvider popupProvider) {
            this.popupProvider = popupProvider;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            getTextField().requestFocus();
            TextFieldPopupProvider activePopupProvider = getActivePopupProvider();
            if (activePopupProvider == null || activePopupProvider != popupProvider) {
                hideActivePopup();
                popupProvider.showPopup();
            }
        }
    }

    @Override
    protected void disposeInner() {
        parentComponent = null;
    }
}
