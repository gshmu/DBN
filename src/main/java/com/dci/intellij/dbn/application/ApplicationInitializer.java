package com.dci.intellij.dbn.application;

import com.dci.intellij.dbn.common.compatibility.Todo;
import com.dci.intellij.dbn.connection.ConnectionCache;
import com.intellij.ide.ApplicationInitializedListener;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

@Todo
public class ApplicationInitializer implements ApplicationInitializedListener, DumbAware {

    public void init(@NotNull Project project) {
/*


        DatabaseConsoleManager.getInstance(project);
        EditorStateManager.getInstance(project);
        SourceCodeManager.getInstance(project);
        DatasetEditorManager.getInstance(project);
        DatabaseFileManager.getInstance(project);
        DatabaseLoaderManager.getInstance(project);
*/
    }

    @Override
    public void componentsInitialized() {
        ConnectionCache.getInstance();
    }
}
