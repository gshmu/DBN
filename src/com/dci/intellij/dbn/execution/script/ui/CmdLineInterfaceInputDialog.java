package com.dci.intellij.dbn.execution.script.ui;

import javax.swing.Action;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.execution.script.CmdLineInterface;
import com.intellij.openapi.project.Project;

public class CmdLineInterfaceInputDialog extends DBNDialog<CmdLineInterfaceInputForm> {
    private CmdLineInterface cmdLineInterface = new CmdLineInterface();

    public CmdLineInterfaceInputDialog(Project project, @NotNull CmdLineInterface cmdLineInterface, Set<String> usedNames) {
        super(project, "Add Command-Line Interface", true);
        setModal(true);
        component = new CmdLineInterfaceInputForm(this, cmdLineInterface, usedNames);
        Action okAction = getOKAction();
        okAction.putValue(Action.NAME, "Save");
        init();
    }

    @Override
    protected String getDimensionServiceKey() {
        return null;
    }

    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
                getOKAction(),
                getCancelAction(),
        };
    }

    public void setActionEnabled(boolean enabled) {
        getOKAction().setEnabled(enabled);
    }

    public CmdLineInterface getCmdLineInterface() {
        return cmdLineInterface;
    }

    public void setCmdLineInterface(CmdLineInterface cmdLineInterface) {
        this.cmdLineInterface = cmdLineInterface;
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
