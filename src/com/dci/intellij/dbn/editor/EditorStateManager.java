package com.dci.intellij.dbn.editor;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.browser.options.DatabaseBrowserEditorSettings;
import com.dci.intellij.dbn.browser.options.DatabaseBrowserSettings;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.environment.EnvironmentManager;
import com.dci.intellij.dbn.common.environment.options.listener.EnvironmentManagerListener;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.options.setting.SettingsSupport;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.editor.code.SourceCodeEditor;
import com.dci.intellij.dbn.editor.code.SourceCodeManagerAdapter;
import com.dci.intellij.dbn.editor.code.SourceCodeManagerListener;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.editor.DefaultEditorOption;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.dci.intellij.dbn.vfs.file.DBContentVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import gnu.trove.THashMap;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

@State(
    name = EditorStateManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class EditorStateManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {
    public static final String COMPONENT_NAME = "DBNavigator.Project.EditorStateManager";

    private final Map<DBObjectType, EditorProviderId> lastUsedEditorProviders = new THashMap<>();
    private EditorStateManager(Project project) {
        super(project);

        ProjectEvents.subscribe(project, this, SourceCodeManagerListener.TOPIC, sourceCodeManagerListener);
        ProjectEvents.subscribe(project, this, FileEditorManagerListener.FILE_EDITOR_MANAGER, fileEditorListener);
        ProjectEvents.subscribe(project, this, EnvironmentManagerListener.TOPIC, environmentManagerListener);
    }

    public static EditorStateManager getInstance(@NotNull Project project) {
        return Failsafe.getComponent(project, EditorStateManager.class);
    }

    private final SourceCodeManagerListener sourceCodeManagerListener = new SourceCodeManagerAdapter() {
        @Override
        public void sourceCodeLoaded(@NotNull final DBSourceCodeVirtualFile sourceCodeFile, boolean initialLoad) {
            Project project = getProject();
            EnvironmentManager environmentManager = EnvironmentManager.getInstance(project);
            boolean readonly = environmentManager.isReadonly(sourceCodeFile);
            EditorUtil.setEditorsReadonly(sourceCodeFile, readonly);
        }
    };

    private final EnvironmentManagerListener environmentManagerListener = new EnvironmentManagerListener() {
        @Override
        public void configurationChanged(Project project) {
            FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
            EnvironmentManager environmentManager = EnvironmentManager.getInstance(project);
            VirtualFile[] openFiles = fileEditorManager.getOpenFiles();
            for (VirtualFile virtualFile : openFiles) {
                if (virtualFile instanceof DBEditableObjectVirtualFile) {
                    DBEditableObjectVirtualFile editableDatabaseFile = (DBEditableObjectVirtualFile) virtualFile;
                    if (editableDatabaseFile.isContentLoaded()) {
                        List<DBContentVirtualFile> contentFiles = editableDatabaseFile.getContentFiles();
                        for (DBContentVirtualFile contentFile : contentFiles) {
                            boolean readonly = environmentManager.isReadonly(contentFile);
                            EditorUtil.setEditorsReadonly(contentFile, readonly);
                        }
                    }
                }
            }
        }
    };

    @Nullable
    public EditorProviderId getEditorProvider(DBObjectType objectType) {
        DatabaseBrowserSettings browserSettings = ProjectSettingsManager.getSettings(getProject()).getBrowserSettings();
        DatabaseBrowserEditorSettings editorSettings = browserSettings.getEditorSettings();
        DefaultEditorOption option = editorSettings.getOption(objectType);
        if (option != null) {
            switch (option.getEditorType()) {
                case SPEC: return EditorProviderId.CODE_SPEC;
                case BODY: return EditorProviderId.CODE_BODY;
                case CODE: return EditorProviderId.CODE;
                case DATA: return EditorProviderId.DATA;
                case SELECTION: return lastUsedEditorProviders.get(objectType);
            }
        }

        return null;
    }

    /****************************************
    *             ProjectComponent          *
    *****************************************/
    @Override
    @NonNls
    @NotNull
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    private final FileEditorManagerListener fileEditorListener = new FileEditorManagerListener() {
        @Override
        public void selectionChanged(@NotNull FileEditorManagerEvent event) {
            DBObject oldObject = null;
            DBObject newObject = null;
            EditorProviderId editorProviderId = null;


            FileEditor oldEditor = event.getOldEditor();
            if (oldEditor instanceof SourceCodeEditor) {
                SourceCodeEditor sourceCodeEditor = (SourceCodeEditor) oldEditor;
                oldObject = sourceCodeEditor.getObject();
            } else if (oldEditor instanceof DatasetEditor) {
                DatasetEditor datasetEditor = (DatasetEditor) oldEditor;
                oldObject = datasetEditor.getDataset();
            }

            FileEditor newEditor = event.getNewEditor();
            if (newEditor instanceof SourceCodeEditor) {
                SourceCodeEditor sourceCodeEditor = (SourceCodeEditor) newEditor;
                editorProviderId = sourceCodeEditor.getEditorProviderId();
                newObject = sourceCodeEditor.getObject();
            } else if (newEditor instanceof DatasetEditor) {
                DatasetEditor datasetEditor = (DatasetEditor) newEditor;
                newObject = datasetEditor.getDataset();
                editorProviderId = EditorProviderId.DATA;
            }

            if (editorProviderId != null && oldObject != null && newObject != null && newObject.equals(oldObject)) {
                DBObjectType objectType = newObject.getObjectType();
                lastUsedEditorProviders.put(objectType, editorProviderId);
            }
        }
    };

    /****************************************
     *       PersistentStateComponent       *
     *****************************************/
    @Nullable
    @Override
    public Element getState() {
        Element element = new Element("state");
        Element editorProvidersElement = new Element("last-used-providers");
        element.addContent(editorProvidersElement);
        for (DBObjectType objectType : lastUsedEditorProviders.keySet()) {
            Element objectTypeElement = new Element("object-type");
            EditorProviderId editorProviderId = lastUsedEditorProviders.get(objectType);
            SettingsSupport.setEnumAttribute(objectTypeElement, "object-type", objectType);
            SettingsSupport.setEnumAttribute(objectTypeElement, "editor-provider", editorProviderId);
            editorProvidersElement.addContent(objectTypeElement);
        }
        return element;
    }

    @Override
    public void loadState(Element element) {
        lastUsedEditorProviders.clear();
        Element editorProvidersElement = element.getChild("last-used-providers");
        if (editorProvidersElement != null) {
            for (Element objectTypeElement : editorProvidersElement.getChildren()) {
                DBObjectType objectType = SettingsSupport.getEnumAttribute(objectTypeElement, "object-type", DBObjectType.class);
                EditorProviderId editorProviderId = SettingsSupport.getEnumAttribute(objectTypeElement, "editor-provider", EditorProviderId.class);
                lastUsedEditorProviders.put(objectType, editorProviderId);
            }
        }
/*
        recordViewColumnSortingType = SettingsUtil.getEnum(element, "record-view-column-sorting-type", recordViewColumnSortingType);
        valuePreviewTextWrapping = SettingsUtil.getBoolean(element, "value-preview-text-wrapping", valuePreviewTextWrapping);
        valuePreviewTextWrapping = SettingsUtil.getBoolean(element, "value-preview-pinned", valuePreviewPinned);
*/
    }

}
