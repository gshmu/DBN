package com.dci.intellij.dbn.connection.config.tns.ui;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.connection.config.tns.TnsName;
import com.intellij.openapi.project.Project;

public class TnsNamesImportDialog extends DBNDialog<TnsNamesImportForm> {
    private List<TnsName> tnsNames;
    private ImportAllAction importAllAction = new ImportAllAction();
    private ImportSelectedAction importSelectedAction = new ImportSelectedAction();

    public TnsNamesImportDialog(Project project, @Nullable File file) {
        super(project, "Import TNS Names", true);
        setModal(true);
        component = new TnsNamesImportForm(this, file);
        init();
    }

    public AbstractAction getImportSelectedAction() {
        return importSelectedAction;
    }

    public AbstractAction getImportAllAction() {
        return importAllAction;
    }

    public List<TnsName> getTnsNames() {
        return tnsNames;
    }

    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
                importSelectedAction,
                importAllAction,
                getCancelAction(),
        };
    }

    private class ImportAllAction extends AbstractAction {
        private ImportAllAction() {
            super("Import All");
        }

        public void actionPerformed(ActionEvent e) {
            tnsNames = component.getAllTnsNames();
            doOKAction();
        }
    }
    
    private class ImportSelectedAction extends AbstractAction {
        private ImportSelectedAction() {
            super("Import Selected");
        }

        public void actionPerformed(ActionEvent e) {
            tnsNames = component.getSelectedTnsNames();
            doOKAction();
        }
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
