package com.dci.intellij.dbn.project;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

public class ProjectWorkspaceInitializer implements StartupActivity, DumbAware {

    public ProjectWorkspaceInitializer() {
    }

    @Override
    public void runActivity(@NotNull Project project) {
        // TODO SERVICES
/*
        DatabaseConsoleManager.getInstance(project);
        EditorStateManager.getInstance(project);
        SourceCodeManager.getInstance(project);
        DatasetEditorManager.getInstance(project);
        DatabaseLoaderManager.getInstance(project);

        DatabaseFileManager databaseFileManager = DatabaseFileManager.getInstance(project);
        databaseFileManager.setProjectInitialized(true);
        databaseFileManager.reopenDatabaseEditors();
*/
    }
}
