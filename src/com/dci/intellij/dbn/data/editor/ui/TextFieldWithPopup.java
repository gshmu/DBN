package com.dci.intellij.dbn.data.editor.ui;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.KeyUtil;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;

public class TextFieldWithPopup extends JPanel implements DataEditorComponent {
    private JTextField textField;
    private JLabel button;

    private List<TextFieldPopupProviderForm> popupProviders = new ArrayList<TextFieldPopupProviderForm>();
    private UserValueHolder userValueHolder;
    private boolean showsButton;
    private Project project;

    public TextFieldWithPopup(Project project) {
        super(new BorderLayout(2, 0));
        this.project = project;
        setMaximumSize(new Dimension(-1, 24));
        setPreferredSize(new Dimension(-1, 24));
        setMinimumSize(new Dimension(180, 24));

        textField = new JTextField();
        textField.setMargin(new Insets(0, 1, 0, 1));
        add(textField, BorderLayout.CENTER);

        button = new JLabel(Icons.DATA_EDITOR_BROWSE);
        button.setBorder(BUTTON_BORDER);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(mouseListener);

        Shortcut[] shortcuts = KeyUtil.getShortcuts(IdeActions.ACTION_SHOW_INTENTION_ACTIONS);
        String shortcutText = KeymapUtil.getShortcutsText(shortcuts);
        button.setToolTipText("Open editor (" + shortcutText + ")");
        add(button, BorderLayout.EAST);
        textField.setPreferredSize(new Dimension(150, 24));
        textField.setMaximumSize(new Dimension(-1, 24));
        textField.addKeyListener(keyListener);
        textField.addFocusListener(focusListener);

        customizeButton(button);
        customizeTextField(textField);
    }

    public Project getProject() {
        return project;
    }

    public void setEditable(boolean editable){
        textField.setEditable(editable);
    }
                                                                                  
    public void setUserValueHolder(UserValueHolder userValueHolder) {
        this.userValueHolder = userValueHolder;
    }

    private void updateButtonToolTip() {
        if (popupProviders.size() == 1) {
            TextFieldPopupProviderForm popupProvider = getDefaultPopupProvider();
            String toolTipText = "Open " + popupProvider.getDescription();
            String keyShortcutDescription = popupProvider.getKeyShortcutDescription();
            if (keyShortcutDescription != null) {
                toolTipText += " (" + keyShortcutDescription + ")";
            }
            button.setToolTipText(toolTipText);
        }
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

    @Override
    public void setEnabled(boolean enabled) {
        //textField.setEnabled(enabled);
        textField.setEditable(enabled);
        button.setVisible(showsButton && enabled);
    }

    /******************************************************
     *                    PopupProviders                  *
     ******************************************************/
    public void createValuesListPopup(List<String> valuesList, boolean useDynamicFiltering) {
        ValuesListPopupProviderForm popupProviderForm = new ValuesListPopupProviderForm(this, valuesList, useDynamicFiltering);
        addPopupProvider(popupProviderForm);
        updateButtonToolTip();
    }

    public void createValuesListPopup(ListPopupValuesProvider valuesProvider, boolean useDynamicFiltering) {
        ValuesListPopupProviderForm popupProviderForm = new ValuesListPopupProviderForm(this, valuesProvider, useDynamicFiltering);
        addPopupProvider(popupProviderForm);
        updateButtonToolTip();
    }

    public void createTextAreaPopup(boolean autoPopup) {
        TextEditorPopupProviderForm popupProviderForm = new TextEditorPopupProviderForm(this, autoPopup);
        addPopupProvider(popupProviderForm);
        updateButtonToolTip();
        showsButton = true;
        button.setVisible(true);
    }

    public void createCalendarPopup(boolean autoPopup) {
        CalendarPopupProviderForm popupProviderForm = new CalendarPopupProviderForm(this, autoPopup);
        addPopupProvider(popupProviderForm);
        updateButtonToolTip();
        showsButton = true;
        button.setVisible(true);
    }

    private void addPopupProvider(TextFieldPopupProviderForm popupProviderForm) {
        popupProviders.add(popupProviderForm);
        Disposer.register(this, popupProviderForm);
    }

    public void setPopupEnabled(TextFieldPopupType popupType, boolean enabled) {
        for (TextFieldPopupProviderForm popupProvider : popupProviders) {
            if (popupProvider.getPopupType() == popupType) {
                popupProvider.setEnabled(enabled);
                if (popupProvider == getDefaultPopupProvider()) {
                    button.setVisible(enabled);
                }
                break;
            }
        }
    }

    public void disposeActivePopup() {
        TextFieldPopupProviderForm popupProvider = getActivePopupProvider();
        if ( popupProvider != null) {
             popupProvider.hidePopup();
        }
    }

    public TextFieldPopupProviderForm getAutoPopupProvider() {
        for (TextFieldPopupProviderForm popupProvider : popupProviders) {
            if (popupProvider.isAutoPopup()) {
                return popupProvider;
            }
        }
        return null;
    }

    public TextFieldPopupProviderForm getDefaultPopupProvider() {
        return popupProviders.get(0);
    }

    public TextFieldPopupProviderForm getActivePopupProvider() {
        for (TextFieldPopupProviderForm popupProvider : popupProviders) {
            if (popupProvider.isShowingPopup()) {
                return popupProvider;
            }
        }
        return null;
    }

    public TextFieldPopupProviderForm getPopupProvider(KeyEvent keyEvent) {
        for (TextFieldPopupProviderForm popupProvider : popupProviders) {
            if (popupProvider.matchesKeyEvent(keyEvent)) {
                return popupProvider;
            }
        }
        return null;
    }

    /********************************************************
     *                    FocusListener                     *
     ********************************************************/
    private FocusListener focusListener = new FocusAdapter() {
        @Override
        public void focusLost(FocusEvent focusEvent) {
            TextFieldPopupProviderForm popupProvider = getActivePopupProvider();
            if (popupProvider != null) {
                popupProvider.handleFocusLostEvent(focusEvent);
            }
        }
    };

    /********************************************************
     *                      KeyListener                     *
     ********************************************************/
    private KeyListener keyListener = new KeyAdapter() {
        public void keyPressed(KeyEvent keyEvent) {
            TextFieldPopupProviderForm popupProvider = getActivePopupProvider();
            if (popupProvider != null) {
                popupProvider.handleKeyPressedEvent(keyEvent);

            } else {
                popupProvider = getPopupProvider(keyEvent);
                if (popupProvider != null && popupProvider.isEnabled()) {
                    disposeActivePopup();
                    popupProvider.showPopup();
                }
            }
        }

        public void keyReleased(KeyEvent keyEvent) {
            TextFieldPopupProviderForm popupProviderForm = getActivePopupProvider();
            if (popupProviderForm != null) {
                popupProviderForm.handleKeyReleasedEvent(keyEvent);

            }
        }
    };
    /********************************************************
     *                    ActionListener                    *
     ********************************************************/
    private ActionListener actionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            TextFieldPopupProviderForm defaultPopupProvider = getDefaultPopupProvider();
            TextFieldPopupProviderForm popupProvider = getActivePopupProvider();
            if (popupProvider == null || popupProvider != defaultPopupProvider) {
                disposeActivePopup();
                defaultPopupProvider.showPopup();
            }
        }
    };

    private MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            TextFieldPopupProviderForm defaultPopupProvider = getDefaultPopupProvider();
            TextFieldPopupProviderForm popupProvider = getActivePopupProvider();
            if (popupProvider == null || popupProvider != defaultPopupProvider) {
                disposeActivePopup();
                defaultPopupProvider.showPopup();
            }
        }
    };

    public UserValueHolder getUserValueHolder() {
        return userValueHolder;
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
        if (!isDisposed()) {
            disposed = true;
            userValueHolder = null;
        }
    }

}
