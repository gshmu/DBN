package com.dci.intellij.dbn.ddl.ui;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class AttachDDLFileDialog extends DBNDialog<SelectDDLFileForm> {
    private DBObjectRef<DBSchemaObject> objectRef;
    private boolean showLookupOption;
    private List<VirtualFile> virtualFiles;

    public AttachDDLFileDialog(List<VirtualFile> virtualFiles, @NotNull DBSchemaObject object, boolean showLookupOption) {
        super(object.getProject(), "Attach DDL file", true);
        this.virtualFiles = virtualFiles;
        this.objectRef = DBObjectRef.from(object);
        this.showLookupOption = showLookupOption;
        getOKAction().putValue(Action.NAME, "Attach selected");
        init();
    }

    @NotNull
    @Override
    protected SelectDDLFileForm createComponent() {
        DBSchemaObject object = getObject();
        String typeName = object.getTypeName();
        String hint =
                "Following DDL files were found matching the name of the selected " + typeName + ". " +
                        "Select the files to attach to this object.\n" +
                        "NOTE: Attached DDL files will become readonly and their content will change automatically when the " + typeName + " is edited.";
        return new SelectDDLFileForm(object, virtualFiles, hint, showLookupOption);
    }

    @Override
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
        return DBObjectRef.ensure(objectRef);
    }

    private class SelectAllAction extends AbstractAction {
        private SelectAllAction() {
            super("Attach all");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            getComponent().selectAll();
            doOKAction();
        }
    }

    private class SelectNoneAction extends AbstractAction {
        private SelectNoneAction() {
            super("Attach none");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            SelectDDLFileForm component = getComponent();
            component.selectNone();
            if (showLookupOption && component.isDoNotPromptSelected()) {
                ConnectionHandler connectionHandler = getObject().getConnectionHandler();
                connectionHandler.getSettings().getDetailSettings().setEnableDdlFileBinding(false);
            }
            close(2);
        }
    }

    @Override
    protected void doOKAction() {
        SelectDDLFileForm component = getComponent();
        DBSchemaObject object = getObject();
        Project project = object.getProject();
        DDLFileAttachmentManager fileAttachmentManager = DDLFileAttachmentManager.getInstance(project);
        Object[] selectedPsiFiles = component.getSelection();
        for (Object selectedPsiFile : selectedPsiFiles) {
            VirtualFile virtualFile = (VirtualFile) selectedPsiFile;
            fileAttachmentManager.attachDDLFile(object.getRef(), virtualFile);
        }
        if (showLookupOption && component.isDoNotPromptSelected()) {
            ConnectionHandler connectionHandler = object.getConnectionHandler();
            connectionHandler.getSettings().getDetailSettings().setEnableDdlFileBinding(false);
        }

        super.doOKAction();
    }
}
