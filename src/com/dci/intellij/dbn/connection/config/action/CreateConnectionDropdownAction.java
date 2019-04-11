package com.dci.intellij.dbn.connection.config.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.common.action.GroupPopupAction;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class CreateConnectionDropdownAction extends GroupPopupAction {
    private AnAction[] actions = new AnAction[] {
            new CreateConnectionAction(DatabaseType.ORACLE),
            new CreateConnectionAction(DatabaseType.MYSQL),
            new CreateConnectionAction(DatabaseType.POSTGRES),
            new CreateConnectionAction(DatabaseType.SQLITE),
            new CreateConnectionAction(null),
            ActionUtil.SEPARATOR,
            new TnsNamesImportAction()
    };

    public CreateConnectionDropdownAction() {
        super("New Connection", null, Icons.ACTION_ADD_MORE);
    }

    public CreateConnectionDropdownAction(String name, @Nullable String groupTitle, @Nullable Icon icon) {
        super(name, groupTitle, icon);
    }

    @Override
    public DataProvider getDataProvider(AnActionEvent e) {
        return e.getData((DBNDataKeys.CONNECTION_BUNDLE_SETTINGS));
    }

    @Override
    protected AnAction[] getActions(AnActionEvent e) {
        return actions;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        DataProvider dataProvider = getDataProvider(e);
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(dataProvider != null);
        presentation.setText("New Connection");
    }
}
