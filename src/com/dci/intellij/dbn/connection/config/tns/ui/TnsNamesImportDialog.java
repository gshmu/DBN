package com.dci.intellij.dbn.connection.config.tns.ui;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.connection.config.tns.TnsName;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

public class TnsNamesImportDialog extends DBNDialog<TnsNamesImportForm> {
    private WeakRef<List<TnsName>> tnsNames;  // TODO dialog result - Disposable.nullify(...)
    private ImportAllAction importAllAction = new ImportAllAction();
    private ImportSelectedAction importSelectedAction = new ImportSelectedAction();
    private File file;

    public TnsNamesImportDialog(Project project, @Nullable File file) {
        super(project, "Import TNS names", true);
        this.file = file;
        setModal(true);
        init();
    }

    @NotNull
    @Override
    protected TnsNamesImportForm createComponent() {
        return new TnsNamesImportForm(this, file);
    }

    AbstractAction getImportSelectedAction() {
        return importSelectedAction;
    }

    AbstractAction getImportAllAction() {
        return importAllAction;
    }

    public List<TnsName> getTnsNames() {
        return WeakRef.get(tnsNames);
    }

    @Override
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

        @Override
        public void actionPerformed(ActionEvent e) {
            tnsNames = WeakRef.from(getComponent().getAllTnsNames());
            doOKAction();
        }
    }
    
    private class ImportSelectedAction extends AbstractAction {
        private ImportSelectedAction() {
            super("Import Selected");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            tnsNames = WeakRef.from(getComponent().getSelectedTnsNames());
            doOKAction();
        }
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }
}
