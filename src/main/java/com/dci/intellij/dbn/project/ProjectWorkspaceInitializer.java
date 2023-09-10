package com.dci.intellij.dbn.project;

import com.dci.intellij.dbn.common.component.ProjectComponentBase;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.thread.Background;
import com.dci.intellij.dbn.connection.console.DatabaseConsoleManager;
import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.debugger.ExecutionConfigManager;
import com.dci.intellij.dbn.editor.DatabaseEditorStateManager;
import com.dci.intellij.dbn.editor.DatabaseFileEditorManager;
import com.dci.intellij.dbn.editor.code.SourceCodeManager;
import com.dci.intellij.dbn.editor.data.DatasetEditorManager;
import com.dci.intellij.dbn.execution.compiler.DatabaseCompilerManager;
import com.dci.intellij.dbn.language.common.DBLanguageFileType;
import com.dci.intellij.dbn.object.common.loader.DatabaseLoaderManager;
import com.dci.intellij.dbn.vfs.DBVirtualFile;
import com.dci.intellij.dbn.vfs.DatabaseFileManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.common.component.Components.projectService;

/**
 * TODO SERVICES
 * TODO find another way to define "silent" dependencies
 */
@Getter
public class ProjectWorkspaceInitializer extends ProjectComponentBase implements /*StartupActivity, */DumbAware {
    public static final String COMPONENT_NAME = "DBNavigator.Project.WorkspaceInitializer";
    private boolean initialized;


    public ProjectWorkspaceInitializer(Project project) {
        super(project, COMPONENT_NAME);
        ProjectEvents.subscribe(FileEditorManagerListener.Before.FILE_EDITOR_MANAGER, componentInitializer());

        performCleanup();
        reopenEditors();
    }

    @NotNull
    public static ProjectWorkspaceInitializer getInstance(@NotNull Project project) {
        return projectService(project, ProjectWorkspaceInitializer.class);
    }

    public static void init(@NotNull Project project) {
        getInstance(project);
    }

    private void performCleanup() {
        Project project = getProject();
        Background.run(project, () -> {
            ExecutionConfigManager configManager = ExecutionConfigManager.getInstance(project);
            configManager.removeRunConfigurations();
        });
    }

    private void reopenEditors() {
        DatabaseFileManager fileManager = DatabaseFileManager.getInstance(getProject());
        fileManager.reopenDatabaseEditors();
    }


    private FileEditorManagerListener.Before componentInitializer() {
        return new FileEditorManagerListener.Before() {
            @Override
            public void beforeFileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                Project project = source.getProject();

                ProjectWorkspaceInitializer initializer = getInstance(project);
                if (initializer.shouldInitialize(file)) initializer.initializeComponents();
            }
        };
    }

    private boolean shouldInitialize(VirtualFile file) {
        if (initialized) return false;
        if (file instanceof DBVirtualFile) return true;
        if (file.getFileType() instanceof DBLanguageFileType) return true;
        return false;
    }

    public void initializeComponents() {
        Project project = getProject();
        DatabaseConsoleManager.getInstance(project);
        DatabaseEditorStateManager.getInstance(project);
        SourceCodeManager.getInstance(project);
        DatasetEditorManager.getInstance(project);
        DatabaseCompilerManager.getInstance(project);
        DDLFileAttachmentManager.getInstance(project);
        DatabaseLoaderManager.getInstance(project);
        DatabaseFileEditorManager.getInstance(project);
        initialized = true;
    }
}
