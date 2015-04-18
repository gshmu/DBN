package com.dci.intellij.dbn.connection.config.action;

import javax.swing.Icon;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.common.action.GroupPopupAction;
import com.dci.intellij.dbn.common.util.DataProviderSupplier;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class CreateConnectionDropdownAction extends GroupPopupAction {
    private CreateConnectionAction[] actions = new CreateConnectionAction[] {
            new CreateConnectionAction(DatabaseType.ORACLE),
            new CreateConnectionAction(DatabaseType.MYSQL),
            new CreateConnectionAction(DatabaseType.POSTGRES),
            new CreateConnectionAction(null)
    };

    public CreateConnectionDropdownAction() {
        super("Create Connection", null, Icons.ACTION_ADD_MORE);
    }

    public CreateConnectionDropdownAction(String name, @Nullable String groupTitle, @Nullable Icon icon) {
        super(name, groupTitle, icon);
    }

    @Override
    public DataProviderSupplier getDataProviderSupplier(AnActionEvent e) {
        return e.getData((DBNDataKeys.CONNECTION_BUNDLE_SETTINGS));
    }

    @Override
    protected AnAction[] getActions(AnActionEvent e) {
        return actions;
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setText("Create Connection");
    }
}
