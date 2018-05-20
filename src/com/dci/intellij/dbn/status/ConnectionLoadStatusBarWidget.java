package com.dci.intellij.dbn.status;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConnectionLoadStatusBarWidget implements StatusBarWidget{
    private Project project;

    public ConnectionLoadStatusBarWidget(Project project) {
        this.project = project;
    }

    @NotNull
    @Override
    public String ID() {
        return "DBNavigator.ConnectionLoadStatus";
    }

    @Nullable
    @Override
    public WidgetPresentation getPresentation(@NotNull PlatformType type) {
        return null;
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {

    }

    @Override
    public void dispose() {

    }
}
