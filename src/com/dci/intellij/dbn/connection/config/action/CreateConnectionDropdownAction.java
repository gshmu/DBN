package com.dci.intellij.dbn.connection.config.action;

import javax.swing.Icon;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.common.action.GroupPopupAction;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.DataProviderSupplier;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;

public class CreateConnectionDropdownAction extends GroupPopupAction {
    private AnAction[] actions = new AnAction[] {
            new CreateConnectionAction(DatabaseType.ORACLE),
            new CreateConnectionAction(DatabaseType.MYSQL),
            new CreateConnectionAction(DatabaseType.POSTGRES),
            ActionUtil.SEPARATOR,
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
        DataProviderSupplier dataProviderSupplier = getDataProviderSupplier(e);
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(dataProviderSupplier != null);
        presentation.setText("Create Connection");
    }
}
