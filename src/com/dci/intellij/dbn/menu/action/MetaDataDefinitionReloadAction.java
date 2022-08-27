package com.dci.intellij.dbn.menu.action;

import com.dci.intellij.dbn.connection.DatabaseInterfaceProviders;
import com.dci.intellij.dbn.diagnostics.Diagnostics;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

public class MetaDataDefinitionReloadAction extends DumbAwareAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DatabaseInterfaceProviders.reset();
    }

    @Override
    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setVisible(Diagnostics.isBulkActionsEnabled());
    }

}
