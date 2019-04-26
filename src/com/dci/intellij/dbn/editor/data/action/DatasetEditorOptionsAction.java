package com.dci.intellij.dbn.editor.data.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.GroupPopupAction;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.action.ProjectSettingsOpenAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class DatasetEditorOptionsAction extends GroupPopupAction {
    public DatasetEditorOptionsAction() {
        super("Options", "Options", Icons.ACTION_OPTIONS);
    }
    @Override
    protected AnAction[] getActions(AnActionEvent e) {
        return new AnAction[]{
                new DataSortingOpenAction(),
                new ColumnSetupOpenAction(),
                ActionUtil.SEPARATOR,
                new ProjectSettingsOpenAction(ConfigId.DATA_EDITOR, false)
        };
    }
}
