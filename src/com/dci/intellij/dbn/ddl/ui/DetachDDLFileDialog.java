package com.dci.intellij.dbn.ddl.ui;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import java.awt.event.ActionEvent;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.vfs.VirtualFile;

public class DetachDDLFileDialog extends DBNDialog {
    private SelectDDLFileForm fileForm;
    private DBSchemaObject object;

    public DetachDDLFileDialog(List<VirtualFile> virtualFiles, DBSchemaObject object) {
        super(object.getProject(), "Detach DDL Files", true);
        this.object = object;
        String hint =
            "Following DDL files are currently attached the selected " + object.getTypeName() + ".\n" +
            "Select files to detach from this object.";
        fileForm = new SelectDDLFileForm(object, virtualFiles, hint, false);
        getOKAction().putValue(Action.NAME, "Detach selected");
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
            super("Detach all");
        }

        public void actionPerformed(ActionEvent e) {
            fileForm.selectAll();
            doOKAction();
        }
    }

    private class SelectNoneAction extends AbstractAction {
        private SelectNoneAction() {
            super("Detach none");
        }

        public void actionPerformed(ActionEvent e) {
            fileForm.selectNone();
            doOKAction();
        }
    }

    protected void doOKAction() {
        DDLFileAttachmentManager fileAttachmentManager = DDLFileAttachmentManager.getInstance(object.getProject());
        Object[] selectedPsiFiles = getSelection();
        for (Object selectedPsiFile : selectedPsiFiles) {
            VirtualFile virtualFile = (VirtualFile) selectedPsiFile;
            fileAttachmentManager.detachDDLFile(virtualFile);
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

    public boolean hasSelection() {
        return getSelection().length > 0;
    }
}
