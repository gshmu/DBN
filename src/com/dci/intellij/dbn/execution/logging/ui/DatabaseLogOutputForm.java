package com.dci.intellij.dbn.execution.logging.ui;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.common.result.ui.ExecutionResultForm;
import com.dci.intellij.dbn.execution.logging.DatabaseLogOutput;
import com.intellij.openapi.util.Disposer;

public class DatabaseLogOutputForm extends DBNFormImpl implements ExecutionResultForm<DatabaseLogOutput>{
    private JPanel mainPanel;
    private JPanel consolePanel;
    private JPanel actionsPanel;

    private DatabaseLogOutput databaseLogOutput;
    private DatabaseLogOutputConsole console;

    public DatabaseLogOutputForm(DatabaseLogOutput databaseLogOutput) {
        ConnectionHandler connectionHandler = databaseLogOutput.getConnectionHandler();
        console = new DatabaseLogOutputConsole(connectionHandler, databaseLogOutput.getReader(), databaseLogOutput.getName());
        consolePanel.add(console.getComponent(), BorderLayout.CENTER);
        Disposer.register(this, console);
    }

    public DatabaseLogOutput getDatabaseLogOutput() {
        return databaseLogOutput;
    }

    public DatabaseLogOutputConsole getConsole() {
        return console;
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    @Override
    public void setExecutionResult(DatabaseLogOutput executionResult) {

    }

    @Override
    public DatabaseLogOutput getExecutionResult() {
        return null;
    }

    @Override
    public void dispose() {
        super.dispose();
        console = null;
        databaseLogOutput = null;
    }
}
