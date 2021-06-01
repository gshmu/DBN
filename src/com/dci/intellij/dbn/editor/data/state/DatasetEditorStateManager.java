package com.dci.intellij.dbn.editor.data.state;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.dci.intellij.dbn.editor.data.state.column.ui.DatasetColumnSetupDialog;
import com.dci.intellij.dbn.editor.data.state.sorting.ui.DatasetEditorSortingDialog;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
    name = DatasetEditorStateManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class DatasetEditorStateManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {

    public static final String COMPONENT_NAME = "DBNavigator.Project.DatasetEditorStateManager";

    private DatasetEditorStateManager(Project project) {
        super(project);
    }


    public static DatasetEditorStateManager getInstance(@NotNull Project project) {
        return Failsafe.getComponent(project, DatasetEditorStateManager.class);
    }

    public void openSortingDialog(@NotNull DatasetEditor datasetEditor) {
        DatasetEditorSortingDialog dialog = new DatasetEditorSortingDialog(datasetEditor);
        dialog.show();
    }

    public void openColumnSetupDialog(@NotNull DatasetEditor datasetEditor) {
        DatasetColumnSetupDialog dialog = new DatasetColumnSetupDialog(datasetEditor);
        dialog.show();
    }

    /***************************************
    *            ProjectComponent           *
    ****************************************/
    @Override
    @NonNls
    @NotNull
    public String getComponentName() {
        return COMPONENT_NAME;
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
