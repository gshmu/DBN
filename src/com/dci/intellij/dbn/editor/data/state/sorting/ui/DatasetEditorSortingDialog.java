package com.dci.intellij.dbn.editor.data.state.sorting.ui;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Action;
import javax.swing.JComponent;

public class DatasetEditorSortingDialog extends DBNDialog {
    private DatasetEditorSortingForm stateForm;
    private DatasetEditor datasetEditor;

    public DatasetEditorSortingDialog(DatasetEditor datasetEditor) {
        super(datasetEditor.getProject(), "Sorting", true);
        this.datasetEditor = datasetEditor;
        setModal(true);
        setResizable(true);
        stateForm = new DatasetEditorSortingForm(datasetEditor);
        getCancelAction().putValue(Action.NAME, "Cancel");
        init();
    }

    protected String getDimensionServiceKey() {
        return "DBNavigator.DatasetColumnSorting";
    }

    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
                getOKAction(),
                getCancelAction(),
                getHelpAction()
        };
    }

    @Override
    protected void doOKAction() {
        stateForm.applyChanges();
        datasetEditor.getEditorTable().sort();
        super.doOKAction();
    }

    @Nullable
    protected JComponent createCenterPanel() {
        return stateForm.getComponent();
    }

    @Override
    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
            stateForm.dispose();
        }
    }
}
