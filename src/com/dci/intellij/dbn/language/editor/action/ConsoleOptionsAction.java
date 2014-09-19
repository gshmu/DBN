package com.dci.intellij.dbn.language.editor.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.GroupPopupAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Separator;

public class ConsoleOptionsAction extends GroupPopupAction {
    public ConsoleOptionsAction() {
        super("Options", "Options", Icons.ACTION_OPTIONS);
    }

    @Override
    protected AnAction[] getActions(AnActionEvent e) {
        return new AnAction[]{
                new RenameConsoleEditorAction(),
                new DeleteConsoleEditorAction(),
                Separator.getInstance(),
                new CreateConsoleEditorAction()
        };
    }
}
