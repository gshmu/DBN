package com.dci.intellij.dbn.execution.logging;

import javax.swing.Icon;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.execution.ExecutionResult;
import com.dci.intellij.dbn.execution.logging.ui.DatabaseLogOutputConsole;
import com.dci.intellij.dbn.execution.logging.ui.DatabaseLogOutputForm;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

public class DatabaseLogOutput implements ExecutionResult {
    private LogOutputRequest request;
    private DatabaseLogOutputForm logOutputForm;

    public DatabaseLogOutput(@NotNull LogOutputRequest request) {
        this.request = request;
    }
    public DatabaseLogOutputForm getForm(boolean create) {
        if (logOutputForm == null && create) {
            logOutputForm = new DatabaseLogOutputForm(getProject(), this);
            Disposer.register(logOutputForm, this);
        }
        return logOutputForm;
    }

    public LogOutputRequest getRequest() {
        return request;
    }

    @Override
    @NotNull
    public String getName() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        VirtualFile sourceFile = request.getSourceFile();
        if (sourceFile == null) {
            DatabaseCompatibilityInterface compatibilityInterface = connectionHandler.getInterfaceProvider().getCompatibilityInterface();
            String databaseLogName = compatibilityInterface.getDatabaseLogName();

            return connectionHandler.getName() + " - " + CommonUtil.nvl(databaseLogName, "Log Output");
        } else {
            return connectionHandler.getName() + " - " + sourceFile.getName();
        }
    }

    public boolean matches(LogOutputRequest request) {
        return this.request == request || this.request.matches(request);
    }

    @Override
    public Icon getIcon() {
        return Icons.EXEC_LOG_OUTPUT_CONSOLE;
    }

    @NotNull
    @Override
    public Project getProject() {
        return getConnectionHandler().getProject();
    }

    @Override
    public String getConnectionId() {
        return request.getConnectionId();
    }

    @Nullable
    public VirtualFile getSourceFile() {
        return request.getSourceFile();
    }

    @NotNull
    @Override
    public ConnectionHandler getConnectionHandler() {
        return request.getConnectionHandler();
    }

    @Override
    public PsiFile createPreviewFile() {
        return null;
    }

    public void write(LogOutputRequest request) {
        this.request = request;
        if (logOutputForm != null && ! logOutputForm.isDisposed()) {
            DatabaseLogOutputConsole console = logOutputForm.getConsole();
            if (request.isAddHeadline()) {
                ConsoleView consoleView = console.getConsole();
                if (consoleView instanceof ConsoleViewImpl) {
                    ConsoleViewImpl consoleViewImpl = (ConsoleViewImpl) consoleView;
                    consoleViewImpl.requestScrollingToEnd();
                }
            }
            console.writeToConsole(request);
        }
    }

    /********************************************************
     *                    Data Provider                     *
     ********************************************************/
    public DataProvider dataProvider = new DataProvider() {
        @Override
        public Object getData(@NonNls String dataId) {
            if (DBNDataKeys.DATABASE_LOG_OUTPUT.is(dataId)) {
                return DatabaseLogOutput.this;
            }
            return null;
        }
    };

    @Nullable
    public DataProvider getDataProvider() {
        return dataProvider;
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
        request = null;
    }
}
