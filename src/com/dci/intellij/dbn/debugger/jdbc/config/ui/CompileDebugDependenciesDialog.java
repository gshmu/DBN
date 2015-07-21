package com.dci.intellij.dbn.debugger.jdbc.config.ui;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.debugger.common.config.DBProgramRunConfiguration;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import org.jetbrains.annotations.NotNull;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

public class CompileDebugDependenciesDialog extends DBNDialog<CompileDebugDependenciesForm> {
    private DBProgramRunConfiguration runConfiguration;
    private List<DBSchemaObject> selection = Collections.emptyList();

    public CompileDebugDependenciesDialog(DBProgramRunConfiguration runConfiguration, List<DBSchemaObject> compileList) {
        super(runConfiguration.getProject(), "Compile Object Dependencies", true);
        this.runConfiguration = runConfiguration;
        this.component = new CompileDebugDependenciesForm(this, runConfiguration, compileList);
        init();
    }

    @Override
    protected String getDimensionServiceKey() {
        return null;
    }

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

        public void actionPerformed(ActionEvent e) {
            doOKAction();
        }
    }

    private class CompileAllAction extends AbstractAction {
        private CompileAllAction() {
            super("Compile all");
        }

        public void actionPerformed(ActionEvent e) {
            component.selectAll();
            doOKAction();
        }
    }

    private class CompileNoneAction extends AbstractAction {
        private CompileNoneAction() {
            super("Compile none");
        }

        public void actionPerformed(ActionEvent e) {
            component.selectNone();
            doOKAction();
        }
    }

    @Override
    protected void doOKAction() {
        selection = component.getSelection();
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
