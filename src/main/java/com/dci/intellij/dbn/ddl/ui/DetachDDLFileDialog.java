package com.dci.intellij.dbn.ddl.ui;

import com.dci.intellij.dbn.common.file.VirtualFileInfo;
import com.dci.intellij.dbn.common.text.TextContent;
import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

import static com.dci.intellij.dbn.common.text.TextContent.plain;

public class DetachDDLFileDialog extends DBNDialog<SelectDDLFileForm> {
    private final List<VirtualFileInfo> fileInfos;
    private final DBObjectRef<DBSchemaObject> objectRef;
    public DetachDDLFileDialog(@NotNull List<VirtualFileInfo> fileInfos, @NotNull DBSchemaObject object) {
        super(object.getProject(), "Detach DDL files", true);
        this.fileInfos = fileInfos;
        this.objectRef = DBObjectRef.of(object);
        renameAction(getOKAction(), "Detach selected");
        setDefaultSize(700, 400);
        init();
    }

    @NotNull
    @Override
    protected SelectDDLFileForm createForm() {
        DBSchemaObject object = objectRef.ensure();
        TextContent hintText = plain(
                "Following DDL files are currently attached the " + object.getQualifiedNameWithType() + ".\n" +
                "Select the files to detach from this object.");
        return new SelectDDLFileForm(this, object, fileInfos, hintText, false);
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

    private class SelectAllAction extends AbstractAction {
        private SelectAllAction() {
            super("Detach all");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            getForm().selectAll();
            doOKAction();
        }
    }

    private class SelectNoneAction extends AbstractAction {
        private SelectNoneAction() {
            super("Detach none");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            getForm().selectNone();
            doOKAction();
        }
    }

    @Override
    protected void doOKAction() {
        DDLFileAttachmentManager fileAttachmentManager = DDLFileAttachmentManager.getInstance(getProject());
        List<VirtualFileInfo> fileInfos = getForm().getSelection();
        for (VirtualFileInfo fileInfo : fileInfos) {
            fileAttachmentManager.detachDDLFile(fileInfo.getFile());
        }
        super.doOKAction();
    }
}
