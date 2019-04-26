package com.dci.intellij.dbn.browser.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.GroupPopupAction;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.action.ProjectSettingsOpenAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class BrowserOptionsAction extends GroupPopupAction {
    public BrowserOptionsAction() {
        super("Options", "Options", Icons.ACTION_OPTIONS);
    }
    @Override
    protected AnAction[] getActions(AnActionEvent e) {
        return new AnAction[]{
                new AutoscrollToEditorAction(),
                new AutoscrollFromEditorAction(),
                ActionUtil.SEPARATOR,
                new ConnectionFilterSettingsOpenAction(),
                ActionUtil.SEPARATOR,
                new ProjectSettingsOpenAction(ConfigId.CONNECTIONS, false)
        };
    }
}
