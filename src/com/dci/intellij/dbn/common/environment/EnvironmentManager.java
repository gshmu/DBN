package com.dci.intellij.dbn.common.environment;

import java.util.Set;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.environment.options.EnvironmentSettings;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.editor.DBEditorTabColorProvider;
import com.dci.intellij.dbn.options.general.GeneralProjectSettings;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.impl.EditorsSplitters;
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

@State(
        name = "DBNavigator.Project.EnvironmentManager",
        storages = {
                @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
                @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/misc.xml", scheme = StorageScheme.DIRECTORY_BASED),
                @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class EnvironmentManager extends AbstractProjectComponent implements PersistentStateComponent<Element>, Disposable {
    private EnvironmentManager(Project project) {
        super(project);
        EventManager.subscribe(project, EnvironmentChangeListener.TOPIC, environmentChangeListener);

    }

    public static EnvironmentManager getInstance(Project project) {
        return project.getComponent(EnvironmentManager.class);
    }
    
    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.EnvironmentManager";
    }

    private EnvironmentChangeListener environmentChangeListener = new EnvironmentChangeListener() {
        @Override
        public void environmentConfigChanged(String environmentTypeId) {
            FileEditorManagerImpl fileEditorManager = (FileEditorManagerImpl) FileEditorManager.getInstance(getProject());
            VirtualFile[] openFiles = fileEditorManager.getOpenFiles();
            Set<EditorsSplitters> splitters = fileEditorManager.getAllSplitters();
            for (VirtualFile virtualFile : openFiles) {
                ConnectionHandler connectionHandler = DBEditorTabColorProvider.getConnectionHandler(virtualFile, getProject());
                if (connectionHandler != null && !connectionHandler.isVirtual() && !connectionHandler.isDisposed() && connectionHandler.getSettings().getDetailSettings().getEnvironmentTypeId().equals(environmentTypeId)) {
                    for (EditorsSplitters splitter : splitters) {
                        splitter.updateFileBackgroundColor(virtualFile);
                    }
                }
            }
        }

        @Override
        public void environmentVisibilitySettingsChanged() {
            FileEditorManagerImpl fileEditorManager = (FileEditorManagerImpl) FileEditorManager.getInstance(getProject());
            VirtualFile[] openFiles = fileEditorManager.getOpenFiles();
            Set<EditorsSplitters> splitters = fileEditorManager.getAllSplitters();
            for (VirtualFile virtualFile : openFiles) {
                for (EditorsSplitters splitter : splitters) {
                    splitter.updateFileBackgroundColor(virtualFile);
                }
            }
        }
    };


    public void dispose() {
        EventManager.unsubscribe(environmentChangeListener);
    }

    public EnvironmentType getEnvironmentType(String id) {
        EnvironmentSettings environmentSettings = GeneralProjectSettings.getInstance(getProject()).getEnvironmentSettings();
        return environmentSettings.getEnvironmentType(id);
    }

    /*********************************************
     *            PersistentStateComponent       *
     *********************************************/
    @Nullable
    @Override
    public Element getState() {
        return null;
    }

    @Override
    public void loadState(Element element) {
    }
}
