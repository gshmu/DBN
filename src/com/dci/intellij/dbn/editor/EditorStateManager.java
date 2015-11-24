package com.dci.intellij.dbn.editor;

import java.util.Map;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.browser.options.DatabaseBrowserEditorSettings;
import com.dci.intellij.dbn.browser.options.DatabaseBrowserSettings;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.editor.code.SourceCodeEditor;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.editor.DefaultEditorOption;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManagerAdapter;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import gnu.trove.THashMap;

@State(
    name = "DBNavigator.Project.EditorStateManager",
    storages = {
        @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
        @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class EditorStateManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {
    private EditorStateManager(Project project) {
        super(project);
    }
    private Map<DBObjectType, EditorProviderId> lastUsedEditorProviders = new THashMap<DBObjectType, EditorProviderId>();

    public static EditorStateManager getInstance(@NotNull Project project) {
        return FailsafeUtil.getComponent(project, EditorStateManager.class);
    }

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
    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.EditorStateManager";
    }

    @Override
    public void initComponent() {
        EventUtil.subscribe(getProject(), this, FileEditorManagerListener.FILE_EDITOR_MANAGER, fileEditorListener);
    }

    FileEditorManagerAdapter fileEditorListener = new FileEditorManagerAdapter() {
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
/*
        SettingsUtil.setEnum(element, "record-view-column-sorting-type", recordViewColumnSortingType);
        SettingsUtil.setBoolean(element, "value-preview-text-wrapping", valuePreviewTextWrapping);
        SettingsUtil.setBoolean(element, "value-preview-pinned", valuePreviewPinned);
*/
        return element;
    }

    @Override
    public void loadState(Element element) {
/*
        recordViewColumnSortingType = SettingsUtil.getEnum(element, "record-view-column-sorting-type", recordViewColumnSortingType);
        valuePreviewTextWrapping = SettingsUtil.getBoolean(element, "value-preview-text-wrapping", valuePreviewTextWrapping);
        valuePreviewTextWrapping = SettingsUtil.getBoolean(element, "value-preview-pinned", valuePreviewPinned);
*/
    }

}
