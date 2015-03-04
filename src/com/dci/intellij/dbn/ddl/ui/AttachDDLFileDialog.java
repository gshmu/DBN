package com.dci.intellij.dbn.ddl.ui;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import java.awt.event.ActionEvent;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.vfs.VirtualFile;

public class AttachDDLFileDialog extends DBNDialog {
    private SelectDDLFileForm fileForm;
    private DBSchemaObject object;
    private boolean showLookupOption;

    public AttachDDLFileDialog(List<VirtualFile> virtualFiles, DBSchemaObject object, boolean showLookupOption) {
        super(object.getProject(), "Attach DDL File", true);
        this.object = object;
        this.showLookupOption = showLookupOption;
        String hint =
            "Following DDL files were found matching the name of the selected " + object.getTypeName() + ".\n" +
            "Select files to attach to this object.\n\n" +
            "NOTE: \nAttached DDL files will become readonly and their content will change automatically when the database object is edited.";
        fileForm = new SelectDDLFileForm(object, virtualFiles, hint, showLookupOption);
        getOKAction().putValue(Action.NAME, "Attach selected");
        init();
    }

    protected String getDimensionServiceKey() {
        return "DBNavigator.DDLFileBinding";
    }

    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
                getOKAction(),
                new SelectAllAction(),
                new SelectNoneAction(),
                getCancelAction()
        };
    }

    private class SelectAllAction extends AbstractAction {
        private SelectAllAction() {
            super("Attach all");
        }

        public void actionPerformed(ActionEvent e) {
            fileForm.selectAll();
            doOKAction();
        }
    }

    private class SelectNoneAction extends AbstractAction {
        private SelectNoneAction() {
            super("Attach none");
        }

        public void actionPerformed(ActionEvent e) {
            fileForm.selectNone();
            if (showLookupOption && fileForm.isDoNotPromptSelected()) {
                ConnectionHandler connectionHandler = object.getConnectionHandler();
                if (connectionHandler != null) {
                    connectionHandler.getSettings().getDetailSettings().setEnableDdlFileBinding(false);
                }
            }
            close(2);
        }
    }

    protected void doOKAction() {
        DDLFileAttachmentManager fileAttachmentManager = DDLFileAttachmentManager.getInstance(object.getProject());
        Object[] selectedPsiFiles = getSelection();
        for (Object selectedPsiFile : selectedPsiFiles) {
            VirtualFile virtualFile = (VirtualFile) selectedPsiFile;
            fileAttachmentManager.bindDDLFile(object, virtualFile);
        }
        if (showLookupOption && fileForm.isDoNotPromptSelected()) {
            ConnectionHandler connectionHandler = object.getConnectionHandler();
            if (connectionHandler != null) {
                connectionHandler.getSettings().getDetailSettings().setEnableDdlFileBinding(false);
            }
        }

        super.doOKAction();
    }

    @Nullable
    protected JComponent createCenterPanel() {
        return fileForm.getComponent();
    }

    public Object[] getSelection() {
        return fileForm.getSelection();
    }
}
