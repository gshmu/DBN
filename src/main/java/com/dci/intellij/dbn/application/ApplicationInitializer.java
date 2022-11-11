package com.dci.intellij.dbn.application;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

public class ApplicationInitializer implements StartupActivity, DumbAware {

    @Override
    public void runActivity(@NotNull Project project) {
/*
        ConnectionCache.getInstance();

        DatabaseConsoleManager.getInstance(project);
        EditorStateManager.getInstance(project);
        SourceCodeManager.getInstance(project);
        DatasetEditorManager.getInstance(project);
        DatabaseFileManager.getInstance(project);
        DatabaseLoaderManager.getInstance(project);
*/
    }
}
