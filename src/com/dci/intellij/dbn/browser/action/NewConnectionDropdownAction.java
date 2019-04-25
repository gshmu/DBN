package com.dci.intellij.dbn.browser.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.DataKeys;
import com.dci.intellij.dbn.common.action.GroupPopupAction;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class NewConnectionDropdownAction extends GroupPopupAction {
    private AnAction[] actions = new AnAction[] {
            new NewConnectionAction(DatabaseType.ORACLE),
            new NewConnectionAction(DatabaseType.MYSQL),
            new NewConnectionAction(DatabaseType.POSTGRES),
            new NewConnectionAction(DatabaseType.SQLITE),
            new NewConnectionAction(null),
            ActionUtil.SEPARATOR,
            new TnsNamesImportAction()
    };

    public NewConnectionDropdownAction() {
        super("New Connection", null, Icons.ACTION_ADD_MORE);
    }

    public NewConnectionDropdownAction(String name, @Nullable String groupTitle, @Nullable Icon icon) {
        super(name, groupTitle, icon);
    }

    @Override
    public DataProvider getDataProvider(AnActionEvent e) {
        return e.getData((DataKeys.CONNECTION_BUNDLE_SETTINGS));
    }

    @Override
    protected AnAction[] getActions(AnActionEvent e) {
        return actions;
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        Presentation presentation = e.getPresentation();
        presentation.setText("New Connection");
        presentation.setEnabled(true);
    }
}
