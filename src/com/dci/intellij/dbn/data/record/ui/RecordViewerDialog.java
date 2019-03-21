package com.dci.intellij.dbn.data.record.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.data.record.DatasetRecord;
import com.dci.intellij.dbn.editor.data.DatasetEditorManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class RecordViewerDialog extends DBNDialog<RecordViewerForm> {
    private DatasetRecord record;

    public RecordViewerDialog(Project project, DatasetRecord record) {
        super(project, "View record", true);
        this.record = record; 
        setModal(false);
        setResizable(true);
        getCancelAction().putValue(Action.NAME, "Close");
        init();
    }

    @NotNull
    @Override
    protected RecordViewerForm createComponent() {
        return new RecordViewerForm(this, record);
    }

    @Override
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

    private class OpenInEditorAction extends AbstractAction {
        public OpenInEditorAction() {
            super("Open In Editor", Icons.OBEJCT_EDIT_DATA);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DatasetEditorManager datasetEditorManager = DatasetEditorManager.getInstance(record.getDataset().getProject());
            datasetEditorManager.openDataEditor(record.getFilterInput());
            doCancelAction();
        }
    }
}
