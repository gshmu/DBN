package com.dci.intellij.dbn.data.editor.ui;

import com.dci.intellij.dbn.common.ui.util.Keyboard;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.Shortcut;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;

public interface TextFieldPopupProvider extends Disposable{
    TextFieldPopupType getPopupType();

    void setEnabled(boolean enabled);

    void setButton(@Nullable JLabel button);

    @Nullable
    JLabel getButton();

    boolean isButtonVisible();

    boolean isEnabled();

    boolean isAutoPopup();

    boolean isShowingPopup();

    void showPopup();

    void hidePopup();

    void handleFocusLostEvent(FocusEvent focusEvent);

    void handleKeyPressedEvent(KeyEvent keyEvent);

    void handleKeyReleasedEvent(KeyEvent keyEvent);

    String getDescription();

    String getKeyShortcutDescription();

    @Nullable
    Icon getButtonIcon();

    Shortcut[] getShortcuts();

    default boolean matchesKeyEvent(KeyEvent keyEvent) {
        return Keyboard.match(getShortcuts(), keyEvent);
    }
}
