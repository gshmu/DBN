package com.dci.intellij.dbn.common.environment;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.environment.options.listener.EnvironmentManagerListener;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.object.common.status.DBObjectStatusHolder;
import com.dci.intellij.dbn.vfs.file.DBContentVirtualFile;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
    name = EnvironmentManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class EnvironmentManager extends AbstractProjectComponent implements PersistentStateComponent<Element>, Disposable {

    public static final String COMPONENT_NAME = "DBNavigator.Project.EnvironmentManager";

    private EnvironmentManager(@NotNull Project project) {
        super(project);
        ProjectEvents.subscribe(project, this, EnvironmentManagerListener.TOPIC, environmentManagerListener);
    }

    public static EnvironmentManager getInstance(@NotNull Project project) {
        return Failsafe.getComponent(project, EnvironmentManager.class);
    }

    public boolean isReadonly(@NotNull DBContentVirtualFile contentFile) {
        return isReadonly(contentFile.getObject(), contentFile.getContentType());
    }

    public boolean isReadonly(@NotNull DBSchemaObject schemaObject, @NotNull DBContentType contentType) {
        EnvironmentType environmentType = schemaObject.getEnvironmentType();
        DBObjectStatusHolder objectStatus = schemaObject.getStatus();
        if (contentType == DBContentType.DATA) {
            return environmentType.isReadonlyData() && objectStatus.isNot(contentType, DBObjectStatus.EDITABLE);
        } else {
            return environmentType.isReadonlyCode() && objectStatus.isNot(contentType, DBObjectStatus.EDITABLE);
        }
    }

    public void enableEditing(@NotNull DBSchemaObject schemaObject, @NotNull DBContentType contentType) {
        schemaObject.getStatus().set(contentType, DBObjectStatus.EDITABLE, true);
        DBContentVirtualFile contentFile = schemaObject.getEditableVirtualFile().getContentFile(contentType);
        if (contentFile != null) {
            EditorUtil.setEditorsReadonly(contentFile, false);

            Project project = getProject();
            ProjectEvents.notify(project,
                    EnvironmentManagerListener.TOPIC,
                    (listener) -> listener.editModeChanged(project, contentFile));
        }
    }

    public void disableEditing(@NotNull DBSchemaObject schemaObject, @NotNull DBContentType contentType) {
        schemaObject.getStatus().set(contentType, DBObjectStatus.EDITABLE, false);
        boolean readonly = isReadonly(schemaObject, contentType);
        DBContentVirtualFile contentFile = schemaObject.getEditableVirtualFile().getContentFile(contentType);
        if (contentFile != null) {
            EditorUtil.setEditorsReadonly(contentFile, readonly);

            Project project = getProject();
            ProjectEvents.notify(project,
                    EnvironmentManagerListener.TOPIC,
                    (listener) -> listener.editModeChanged(project, contentFile));
        }
    }


    @Override
    @NonNls
    @NotNull
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    private final EnvironmentManagerListener environmentManagerListener = new EnvironmentManagerListener() {
        @Override
        public void configurationChanged(Project project) {
            FileEditorManagerImpl fileEditorManager = (FileEditorManagerImpl) FileEditorManager.getInstance(getProject());
            VirtualFile[] openFiles = fileEditorManager.getOpenFiles();
            for (VirtualFile virtualFile : openFiles) {
                fileEditorManager.updateFilePresentation(virtualFile);
            }
        }
    };

    /*********************************************
     *            PersistentStateComponent       *
     *********************************************/
    @Nullable
    @Override
    public Element getState() {
        return null;
    }

    @Override
    public void loadState(@NotNull Element element) {
    }
}
