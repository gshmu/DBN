package com.dci.intellij.dbn.debugger.execution.ui;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.debugger.execution.DBProgramRunConfiguration;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBProgram;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import java.awt.event.ActionEvent;
import java.util.List;

public class CompileDebugDependenciesDialog extends DBNDialog {
    private CompileDebugDependenciesForm dependenciesForm;
    private DBProgramRunConfiguration runConfiguration;

    public CompileDebugDependenciesDialog(DBProgramRunConfiguration runConfiguration, List<DBSchemaObject> compileList) {
        super(runConfiguration.getProject(), "Compile Object Dependencies", true);
        this.runConfiguration = runConfiguration;
        DBMethod method = runConfiguration.getMethod();
        DBProgram program = method.getProgram();
        DBSchemaObject selectedObject = program == null ? method : program;
        this.dependenciesForm = new CompileDebugDependenciesForm(compileList, selectedObject);
        init();
    }

    protected String getDimensionServiceKey() {
        return  null;//"DBNavigator.CompileDependencies";
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
            dependenciesForm.selectAll();
            doOKAction();
        }
    }

    private class CompileNoneAction extends AbstractAction {
        private CompileNoneAction() {
            super("Compile none");
        }

        public void actionPerformed(ActionEvent e) {
            dependenciesForm.selectNone();
            doOKAction();
        }
    }

    @Override
    protected void doOKAction() {
        runConfiguration.setCompileDependencies(!dependenciesForm.rememberSelection());
        super.doOKAction();
    }

    @Override
    public void doCancelAction() {
        runConfiguration.setCompileDependencies(!dependenciesForm.rememberSelection());
        super.doCancelAction();
    }

    @Nullable
    protected JComponent createCenterPanel() {
        return dependenciesForm.getComponent();
    }

    public List<DBSchemaObject> getSelection() {
        return dependenciesForm.getSelection();
    }
}
