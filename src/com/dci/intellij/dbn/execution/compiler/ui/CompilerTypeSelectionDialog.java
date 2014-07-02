package com.dci.intellij.dbn.execution.compiler.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.execution.compiler.CompileType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import java.awt.event.ActionEvent;

public class CompilerTypeSelectionDialog extends DBNDialog {
    private CompilerTypeSelectionForm editorForm;
    private CompileType selection;

    public CompilerTypeSelectionDialog(Project project, @Nullable DBSchemaObject object) {
        super(project, "Compile Type", true);
        setModal(true);
        setResizable(false);
        //setVerticalStretch(0);
        editorForm = new CompilerTypeSelectionForm(object);
        init();
    }

    protected String getDimensionServiceKey() {
        return null;//"DBNavigator.CompileType";
    }

    public boolean rememberSelection() {
        return editorForm.rememberSelection();
    }

    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
                new CompileKeep(),
                new CompileNormalAction(),
                new CompileDebugAction(),
                getCancelAction(),
                //getHelpAction()
        };
    }

    private class CompileKeep extends AbstractAction {
        private CompileKeep() {
            super("Keep current");
            //super("Keep current", Icons.OBEJCT_COMPILE_KEEP);
            putValue(DEFAULT_ACTION, Boolean.TRUE);
        }

        public void actionPerformed(ActionEvent e) {
            selection = CompileType.KEEP;
            doOKAction();
        }
    }

    private class CompileNormalAction extends AbstractAction {
        private CompileNormalAction() {
            super("Normal", Icons.OBEJCT_COMPILE);
            //putValue(DEFAULT_ACTION, Boolean.TRUE);
        }

        public void actionPerformed(ActionEvent e) {
            selection = CompileType.NORMAL;
            doOKAction();
        }
    }

    private class CompileDebugAction extends AbstractAction {
        private CompileDebugAction() {
            super("Debug", Icons.OBEJCT_COMPILE_DEBUG);
        }

        public void actionPerformed(ActionEvent e) {
            selection = CompileType.DEBUG;
            doOKAction();
        }
    }

    @Nullable
    protected JComponent createCenterPanel() {
        return editorForm.getComponent();
    }

    public CompileType getSelection() {
        return selection;
    }
}
