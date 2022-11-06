package com.dci.intellij.dbn.editor.data.state;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.component.PersistentState;
import com.dci.intellij.dbn.common.component.ProjectComponentBase;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.dci.intellij.dbn.editor.data.state.column.ui.DatasetColumnSetupDialog;
import com.dci.intellij.dbn.editor.data.state.sorting.ui.DatasetEditorSortingDialog;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dci.intellij.dbn.common.component.Components.projectService;

@State(
    name = DatasetEditorStateManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class DatasetEditorStateManager extends ProjectComponentBase implements PersistentState {

    public static final String COMPONENT_NAME = "DBNavigator.Project.DatasetEditorStateManager";

    private DatasetEditorStateManager(Project project) {
        super(project, COMPONENT_NAME);
    }


    public static DatasetEditorStateManager getInstance(@NotNull Project project) {
        return projectService(project, DatasetEditorStateManager.class);
    }

    public void openSortingDialog(@NotNull DatasetEditor datasetEditor) {
        DatasetEditorSortingDialog dialog = new DatasetEditorSortingDialog(datasetEditor);
        dialog.show();
    }

    public void openColumnSetupDialog(@NotNull DatasetEditor datasetEditor) {
        DatasetColumnSetupDialog dialog = new DatasetColumnSetupDialog(datasetEditor);
        dialog.show();
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
    public void loadState(@NotNull Element element) {
    }

}
