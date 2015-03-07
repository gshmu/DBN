package com.dci.intellij.dbn.editor.data.state.column.ui;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.dci.intellij.dbn.editor.data.DatasetLoadInstructions;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Action;
import javax.swing.JComponent;

public class DatasetColumnSetupDialog extends DBNDialog {
    public static final DatasetLoadInstructions LOAD_INSTRUCTIONS = new DatasetLoadInstructions(true, true, true, true);
    private DatasetColumnSetupForm columnSetupForm;
    private DatasetEditor datasetEditor;

    public DatasetColumnSetupDialog(Project project, DatasetEditor datasetEditor) {
        super(project, "Column Setup", true);
        this.datasetEditor = datasetEditor;
        setModal(true);
        setResizable(true);
        columnSetupForm = new DatasetColumnSetupForm(project, datasetEditor);
        Disposer.register(this, columnSetupForm);
        getCancelAction().putValue(Action.NAME, "Cancel");
        init();
    }

    protected String getDimensionServiceKey() {
        return "DBNavigator.DatasetColumnSetup";
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
        boolean changed = columnSetupForm.applyChanges();
        if (changed) {
            datasetEditor.loadData(LOAD_INSTRUCTIONS);
        }
        super.doOKAction();
    }

    @Nullable
    protected JComponent createCenterPanel() {
        return columnSetupForm.getComponent();
    }

    @Override
    public void dispose() {
        super.dispose();
        datasetEditor = null;
    }
}
