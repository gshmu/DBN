package com.dci.intellij.dbn.connection.console.ui;

import javax.swing.Action;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.console.DatabaseConsoleManager;

public class CreateConsoleDialog extends DBNDialog {
    private CreateConsoleForm createConsoleForm;

    public CreateConsoleDialog(ConnectionHandler connectionHandler) {
        super(connectionHandler.getProject(), "Create SQL Console", true);
        createConsoleForm = new CreateConsoleForm(this, connectionHandler);
        getOKAction().putValue(Action.NAME, "Create");
        init();
    }

    protected String getDimensionServiceKey() {
        return null;//"DBNavigator.CreateSQLConsole";
    }      

    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
            getOKAction(),
            getCancelAction()
        };
    }

    @Override
    protected void doOKAction() {
        DatabaseConsoleManager databaseConsoleManager = DatabaseConsoleManager.getInstance(getProject());
        databaseConsoleManager.createConsole(
                createConsoleForm.getConnectionHandler(),
                createConsoleForm.getConsoleName());
        super.doOKAction();
    }

    @NotNull
    public Action getOKAction() {
        return super.getOKAction();
    }


    @Nullable
    protected JComponent createCenterPanel() {
        return createConsoleForm.getComponent();
    }
}
