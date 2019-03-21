package com.dci.intellij.dbn.debugger.common.config.ui;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.debugger.common.config.DBRunConfig;
import com.dci.intellij.dbn.debugger.common.process.ui.CompileDebugDependenciesForm;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

public class CompileDebugDependenciesDialog extends DBNDialog<CompileDebugDependenciesForm> {
    private DBRunConfig runConfiguration;
    private List<DBSchemaObject> selection = Collections.emptyList();
    private List<DBSchemaObject> compileList;

    public CompileDebugDependenciesDialog(DBRunConfig runConfiguration, List<DBSchemaObject> compileList) {
        super(runConfiguration.getProject(), "Compile object dependencies", true);
        this.runConfiguration = runConfiguration;
        this.compileList = compileList;
        init();
    }

    @NotNull
    @Override
    protected CompileDebugDependenciesForm createComponent() {
        return new CompileDebugDependenciesForm(this, runConfiguration, compileList);
    }

    @Override
    protected String getDimensionServiceKey() {
        return null;
    }

    @Override
    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
                new CompileAllAction(),
                new CompileSelectedAction(),
                new CompileNoneAction(),
                getCancelAction()
        };
    }

    private class CompileSelectedAction extends AbstractAction {
        private CompileSelectedAction() {
            super("Compile selected");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            doOKAction();
        }
    }

    private class CompileAllAction extends AbstractAction {
        private CompileAllAction() {
            super("Compile all");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            getComponent().selectAll();
            doOKAction();
        }
    }

    private class CompileNoneAction extends AbstractAction {
        private CompileNoneAction() {
            super("Compile none");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            getComponent().selectNone();
            doOKAction();
        }
    }

    @Override
    protected void doOKAction() {
        selection = getComponent().getSelection();
        runConfiguration.setCompileDependencies(!isRememberSelection());
        super.doOKAction();
    }

    @Override
    public void doCancelAction() {
        runConfiguration.setCompileDependencies(!isRememberSelection());
        super.doCancelAction();
    }

    public List<DBSchemaObject> getSelection() {
        return selection;
    }
}
