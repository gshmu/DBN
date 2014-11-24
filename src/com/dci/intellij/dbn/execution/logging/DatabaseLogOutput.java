package com.dci.intellij.dbn.execution.logging;

import javax.swing.Icon;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.execution.ExecutionResult;
import com.dci.intellij.dbn.execution.logging.ui.DatabaseLogOutputForm;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.PsiFile;

public class DatabaseLogOutput implements ExecutionResult {
    private ConnectionHandlerRef connectionHandlerRef;
    private DatabaseLogOutputForm logOutputForm;

    public DatabaseLogOutput(ConnectionHandler connectionHandler) {
        this.connectionHandlerRef = connectionHandler.getRef();
    }

    public DatabaseLogOutputForm getForm() {
        if (logOutputForm == null) {
            logOutputForm = new DatabaseLogOutputForm(this);
            Disposer.register(this, logOutputForm);
        }
        return logOutputForm;
    }

    @Override
    public String getName() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        if (connectionHandler != null) {
            DatabaseCompatibilityInterface compatibilityInterface = connectionHandler.getInterfaceProvider().getCompatibilityInterface();
            String databaseLogName = compatibilityInterface.getDatabaseLogName();

            return connectionHandler.getName() + " - " + CommonUtil.nvl(databaseLogName, "Log Output");
        }
        return "Log Output";
    }

    @Override
    public Icon getIcon() {
        return Icons.EXEC_LOG_OUTPUT_CONSOLE;
    }

    @Override
    public Project getProject() {
        return null;
    }

    @Override
    public ConnectionHandler getConnectionHandler() {
        return ConnectionHandlerRef.get(connectionHandlerRef);
    }

    @Override
    public PsiFile createPreviewFile() {
        return null;
    }

    public void write(String string) {
        if (logOutputForm != null && ! logOutputForm.isDisposed()) {
            logOutputForm.getConsole().writeToConsole(string);
        }
    }

    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    private boolean disposed;

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    public void dispose() {
        disposed = true;
    }
}
