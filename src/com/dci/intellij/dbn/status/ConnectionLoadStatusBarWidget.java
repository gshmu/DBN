package com.dci.intellij.dbn.status;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import org.jetbrains.annotations.NotNull;

public class ConnectionLoadStatusBarWidget extends AbstractProjectComponent implements StatusBarWidget{

    public ConnectionLoadStatusBarWidget(Project project) {
        super(project);
    }

    @NotNull
    @Override
    public String ID() {
        return "DBNavigator.ConnectionLoadStatus";
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {

    }
}
