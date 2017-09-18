package com.dci.intellij.dbn.ddl.ui;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.vfs.VirtualFile;

public class DetachDDLFileDialog extends DBNDialog<SelectDDLFileForm> {
    public DetachDDLFileDialog(List<VirtualFile> virtualFiles, DBSchemaObject object) {
        super(object.getProject(), "Detach DDL files", true);
        String hint =
            "Following DDL files are currently attached the selected " + object.getTypeName() + ". " +
            "Select the files to detach from this object.";
        component = new SelectDDLFileForm(object, virtualFiles, hint, false);
        getOKAction().putValue(Action.NAME, "Detach selected");
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

    private class SelectAllAction extends AbstractAction {
        private SelectAllAction() {
            super("Detach all");
        }

        public void actionPerformed(ActionEvent e) {
            component.selectAll();
            doOKAction();
        }
    }

    private class SelectNoneAction extends AbstractAction {
        private SelectNoneAction() {
            super("Detach none");
        }

        public void actionPerformed(ActionEvent e) {
            component.selectNone();
            doOKAction();
        }
    }

    protected void doOKAction() {
        DDLFileAttachmentManager fileAttachmentManager = DDLFileAttachmentManager.getInstance(getProject());
        Object[] selectedPsiFiles = component.getSelection();
        for (Object selectedPsiFile : selectedPsiFiles) {
            VirtualFile virtualFile = (VirtualFile) selectedPsiFile;
            fileAttachmentManager.detachDDLFile(virtualFile);
        }
        super.doOKAction();
    }
}
