package com.dci.intellij.dbn.project;

import com.dci.intellij.dbn.common.component.EagerService;
import com.dci.intellij.dbn.common.component.ProjectComponentBase;
import com.dci.intellij.dbn.common.event.ProjectEvents;
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
import com.dci.intellij.dbn.options.ProjectSettingsProvider;
import com.dci.intellij.dbn.vfs.DBVirtualFile;
import com.dci.intellij.dbn.vfs.DatabaseFileManager;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunManagerListener;
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
public class ProjectComponentsInitializer extends ProjectComponentBase implements /*StartupActivity, */DumbAware, EagerService {
    public static final String COMPONENT_NAME = "DBNavigator.Project.WorkspaceInitializer";
    private boolean initialized;


    public ProjectComponentsInitializer(Project project) {
        super(project, COMPONENT_NAME);
        ProjectSettingsProvider.init(project);
        ProjectEvents.subscribe(FileEditorManagerListener.Before.FILE_EDITOR_MANAGER, componentInitializer());
        ProjectEvents.subscribe(RunManagerListener.TOPIC, runConfigurationCleaner());

        reopenDatabaseEditors();
    }

    private RunManagerListener runConfigurationCleaner() {
        return new RunManagerListener() {
            @Override
            public void stateLoaded(@NotNull RunManager runManager, boolean isFirstLoadState) {
                ExecutionConfigManager configManager = ExecutionConfigManager.getInstance(getProject());
                configManager.removeRunConfigurations();
            }
        };
    }

    @NotNull
    public static ProjectComponentsInitializer getInstance(@NotNull Project project) {
        return projectService(project, ProjectComponentsInitializer.class);
    }

    private void reopenDatabaseEditors() {
        DatabaseFileManager fileManager = DatabaseFileManager.getInstance(getProject());
        fileManager.reopenDatabaseEditors();
    }


    private FileEditorManagerListener.Before componentInitializer() {
        return new FileEditorManagerListener.Before() {
            @Override
            public void beforeFileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                Project project = source.getProject();

                ProjectComponentsInitializer initializer = getInstance(project);
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
