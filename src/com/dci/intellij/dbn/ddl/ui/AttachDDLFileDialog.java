package com.dci.intellij.dbn.ddl.ui;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.vfs.VirtualFile;

public class AttachDDLFileDialog extends DBNDialog<SelectDDLFileForm> {
    private DBObjectRef<DBSchemaObject> objectRef;
    private boolean showLookupOption;

    public AttachDDLFileDialog(List<VirtualFile> virtualFiles, @NotNull DBSchemaObject object, boolean showLookupOption) {
        super(object.getProject(), "Attach DDL File", true);
        this.objectRef = DBObjectRef.from(object);
        this.showLookupOption = showLookupOption;
        String hint =
            "Following DDL files were found matching the name of the selected " + object.getTypeName() + ".\n" +
            "Select files to attach to this object.\n\n" +
            "NOTE: \nAttached DDL files will become readonly and their content will change automatically when the database object is edited.";
        component = new SelectDDLFileForm(object, virtualFiles, hint, showLookupOption);
        getOKAction().putValue(Action.NAME, "Attach selected");
        init();
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

    @NotNull
    public DBSchemaObject getObject() {
        return DBObjectRef.getnn(objectRef);
    }

    private class SelectAllAction extends AbstractAction {
        private SelectAllAction() {
            super("Attach all");
        }

        public void actionPerformed(ActionEvent e) {
            component.selectAll();
            doOKAction();
        }
    }

    private class SelectNoneAction extends AbstractAction {
        private SelectNoneAction() {
            super("Attach none");
        }

        public void actionPerformed(ActionEvent e) {
            component.selectNone();
            if (showLookupOption && component.isDoNotPromptSelected()) {
                ConnectionHandler connectionHandler = getObject().getConnectionHandler();
                connectionHandler.getSettings().getDetailSettings().setEnableDdlFileBinding(false);
            }
            close(2);
        }
    }

    protected void doOKAction() {
        DBSchemaObject object = getObject();
        DDLFileAttachmentManager fileAttachmentManager = DDLFileAttachmentManager.getInstance(object.getProject());
        Object[] selectedPsiFiles = component.getSelection();
        for (Object selectedPsiFile : selectedPsiFiles) {
            VirtualFile virtualFile = (VirtualFile) selectedPsiFile;
            fileAttachmentManager.bindDDLFile(object, virtualFile);
        }
        if (showLookupOption && component.isDoNotPromptSelected()) {
            ConnectionHandler connectionHandler = object.getConnectionHandler();
            connectionHandler.getSettings().getDetailSettings().setEnableDdlFileBinding(false);
        }

        super.doOKAction();
    }
}
