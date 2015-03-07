package com.dci.intellij.dbn.data.record.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.data.record.DatasetRecord;
import com.dci.intellij.dbn.editor.data.DatasetEditorManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import java.awt.event.ActionEvent;

public class RecordViewerDialog extends DBNDialog {
    private RecordViewerForm editorForm;
    private DatasetRecord record;

    public RecordViewerDialog(Project project, DatasetRecord record) {
        super(project, "View Record", true);
        this.record = record; 
        setModal(false);
        setResizable(true);
        editorForm = new RecordViewerForm(this, record);
        getCancelAction().putValue(Action.NAME, "Close");
        init();
    }


    protected String getDimensionServiceKey() {
        return "DBNavigator.DataRecordViewer";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return editorForm.getPreferredFocusedComponent();
    }

    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
                new OpenInEditorAction(),
                getCancelAction(),
                getHelpAction()
        };
    }
    
    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

    @Nullable
    protected JComponent createCenterPanel() {
        return editorForm.getComponent();
    }

    private class OpenInEditorAction extends AbstractAction {
        public OpenInEditorAction() {
            super("Open In Editor", Icons.OBEJCT_EDIT_DATA);
        }

        public void actionPerformed(ActionEvent e) {
            DatasetEditorManager datasetEditorManager = DatasetEditorManager.getInstance(record.getDataset().getProject());
            datasetEditorManager.openDataEditor(record.getFilterInput());
            doCancelAction();
        }
    }

    @Override
    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
            editorForm.dispose();
            editorForm = null;
        }
    }
}
