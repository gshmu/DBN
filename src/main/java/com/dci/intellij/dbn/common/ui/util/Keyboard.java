package com.dci.intellij.dbn.common.ui.util;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.keymap.KeymapUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class Keyboard {
    public interface Key {
        int ENTER = 10;
        int ESCAPE = 27;
        int DELETE = 127;
    }

    public static boolean match(Shortcut[] shortcuts, KeyEvent e) {
        for (Shortcut shortcut : shortcuts) {
            if (shortcut instanceof KeyboardShortcut) {
                KeyboardShortcut keyboardShortcut = (KeyboardShortcut) shortcut;
                KeyStroke shortkutKeyStroke = keyboardShortcut.getFirstKeyStroke();
                KeyStroke eventKeyStroke = KeyStroke.getKeyStrokeForEvent(e);
                if (shortkutKeyStroke.equals(eventKeyStroke)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean match(AnAction action, KeyEvent e) {
        return match(action.getShortcutSet().getShortcuts(), e);
    }

    public static Shortcut[] getShortcuts(String actionId) {
        return KeymapManager.getInstance().getActiveKeymap().getShortcuts(actionId);
    }

    public static ShortcutSet createShortcutSet(int keyCode, int modifiers) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, modifiers);
        Shortcut shortcut = new KeyboardShortcut(keyStroke, null);
        return new CustomShortcutSet(shortcut);
    }

    public static boolean isEmacsKeymap() {
        return KeymapUtil.isEmacsKeymap();
    }

    public static boolean isEmacsKeymap(@Nullable Keymap keymap) {
        return KeymapUtil.isEmacsKeymap(keymap);
    }
}
