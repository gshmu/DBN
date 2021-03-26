package com.dci.intellij.dbn.data.find.action;

import com.dci.intellij.dbn.common.ui.KeyUtil;
import com.dci.intellij.dbn.common.util.Context;
import com.dci.intellij.dbn.data.find.DataSearchComponent;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class CloseOnESCAction extends DataSearchHeaderAction implements DumbAware {
    public CloseOnESCAction(final DataSearchComponent searchComponent, JComponent component) {
        super(searchComponent);

        ArrayList<Shortcut> shortcuts = new ArrayList<>();
        if (KeyUtil.isEmacsKeymap()) {
            shortcuts.add(new KeyboardShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK), null));
            ActionListener actionListener = e -> {
                DataContext dataContext = Context.getDataContext(searchComponent);
                ActionManager actionManager = ActionManager.getInstance();
                AnActionEvent actionEvent = new AnActionEvent(null, dataContext, "", getTemplatePresentation(), actionManager, 2);
                CloseOnESCAction.this.actionPerformed(actionEvent);
            };
            component.registerKeyboardAction(
                    actionListener,
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                    JComponent.WHEN_FOCUSED);
        } else {
            shortcuts.add(new KeyboardShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), null));
        }

        registerCustomShortcutSet(new CustomShortcutSet(shortcuts.toArray(new Shortcut[0])), component);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        getSearchComponent().close();
    }
}
