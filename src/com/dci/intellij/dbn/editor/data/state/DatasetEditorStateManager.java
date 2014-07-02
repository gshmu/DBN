package com.dci.intellij.dbn.editor.data.state;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.dci.intellij.dbn.editor.data.state.column.ui.DatasetColumnSetupDialog;
import com.dci.intellij.dbn.editor.data.state.sorting.ui.DatasetEditorSortingDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class DatasetEditorStateManager extends AbstractProjectComponent implements JDOMExternalizable {
    private DatasetEditorStateManager(Project project) {
        super(project);
    }


    public static DatasetEditorStateManager getInstance(Project project) {
        return project.getComponent(DatasetEditorStateManager.class);
    }

    public void openSortingDialog(DatasetEditor datasetEditor) {
        DatasetEditorSortingDialog dialog = new DatasetEditorSortingDialog(datasetEditor);
        dialog.show();
    }

    public void openColumnSetupDialog(DatasetEditor datasetEditor) {
        DatasetColumnSetupDialog dialog = new DatasetColumnSetupDialog(datasetEditor);
        dialog.show();
    }

    /***************************************
    *            ProjectComponent           *
    ****************************************/
    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.DatasetStateManager";
    }
    public void disposeComponent() {
        super.disposeComponent();
    }

    /*************************************************
    *               JDOMExternalizable              *
    *************************************************/
    public void readExternal(Element element) throws InvalidDataException {
    }

    public void writeExternal(Element element) throws WriteExternalException {
    }

}
