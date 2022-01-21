package com.dci.intellij.dbn.editor.data.record.ui;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.editor.data.model.DatasetEditorModelRow;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.Action;

public class DatasetRecordEditorDialog extends DBNDialog<DatasetRecordEditorForm> {
    private DatasetEditorModelRow row;
    public DatasetRecordEditorDialog(Project project, DatasetEditorModelRow row) {
        super(project, row.getModel().isEditable() ? "Edit record" : "View record", true);
        this.row = row;
        setModal(true);
        setResizable(true);
        renameAction(getCancelAction(), "Close");
        init();
    }

    @NotNull
    @Override
    protected DatasetRecordEditorForm createForm() {
        return new DatasetRecordEditorForm(this, row);
    }

    @Override
    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
                getCancelAction(),
                getHelpAction()
        };
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }
}
