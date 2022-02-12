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
    private final DBObjectRef<DBSchemaObject> objectRef;
    private final boolean showLookupOption;
    private final List<VirtualFile> virtualFiles;

    public AttachDDLFileDialog(List<VirtualFile> virtualFiles, @NotNull DBSchemaObject object, boolean showLookupOption) {
        super(object.getProject(), "Attach DDL file", true);
        this.virtualFiles = virtualFiles;
        this.objectRef = DBObjectRef.of(object);
        this.showLookupOption = showLookupOption;
        renameAction(getOKAction(), "Attach selected");
        setDefaultSize(700, 400);
        init();
    }

    @NotNull
    @Override
    protected SelectDDLFileForm createForm() {
        DBSchemaObject object = getObject();
        String typeName = object.getTypeName();
        String hint =
                "Following DDL files were found matching the name of the " + object.getQualifiedName() + ".\n" +
                        "Select the files to attach to this object.\n\n" +
                        "NOTE: Attached DDL files will become readonly and their content will change automatically when the " + typeName + " is edited.";
        return new SelectDDLFileForm(this, object, virtualFiles, hint, showLookupOption);
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
            getForm().selectAll();
            doOKAction();
        }
    }

    private class SelectNoneAction extends AbstractAction {
        private SelectNoneAction() {
            super("Attach none");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            SelectDDLFileForm component = getForm();
            component.selectNone();
            if (showLookupOption && component.isDoNotPromptSelected()) {
                ConnectionHandler connectionHandler = getObject().getConnection();
                connectionHandler.getSettings().getDetailSettings().setEnableDdlFileBinding(false);
            }
            close(2);
        }
    }

    @Override
    protected void doOKAction() {
        SelectDDLFileForm component = getForm();
        DBSchemaObject object = getObject();
        Project project = object.getProject();
        DDLFileAttachmentManager fileAttachmentManager = DDLFileAttachmentManager.getInstance(project);
        List<VirtualFile> virtualFiles = component.getSelection();
        for (VirtualFile virtualFile : virtualFiles) {
            fileAttachmentManager.attachDDLFile(object.getRef(), virtualFile);
        }
        if (showLookupOption && component.isDoNotPromptSelected()) {
            ConnectionHandler connectionHandler = object.getConnection();
            connectionHandler.getSettings().getDetailSettings().setEnableDdlFileBinding(false);
        }

        super.doOKAction();
    }
}
