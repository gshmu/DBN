package com.dci.intellij.dbn.execution.logging;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.execution.ExecutionResult;
import com.dci.intellij.dbn.execution.logging.ui.DatabaseLogOutputConsole;
import com.dci.intellij.dbn.execution.logging.ui.DatabaseLogOutputForm;
import com.dci.intellij.dbn.execution.script.ScriptExecutionManager;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class DatabaseLogOutput implements ExecutionResult {
    private ConnectionHandlerRef connectionHandlerRef;
    private DatabaseLogOutputForm logOutputForm;
    private VirtualFile sourceFile;


    public DatabaseLogOutput(@NotNull ConnectionHandler connectionHandler) {
        this(connectionHandler, null);
    }
    public DatabaseLogOutput(@NotNull ConnectionHandler connectionHandler, @Nullable VirtualFile sourceFile) {
        this.connectionHandlerRef = connectionHandler.getRef();
        this.sourceFile = sourceFile;
    }

    public DatabaseLogOutputForm getForm(boolean create) {
        if (logOutputForm == null && create) {
            logOutputForm = new DatabaseLogOutputForm(getProject(), this);
            Disposer.register(logOutputForm, this);
        }
        return logOutputForm;
    }

    @Override
    @NotNull
    public String getName() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        if (sourceFile == null) {
            DatabaseCompatibilityInterface compatibilityInterface = connectionHandler.getInterfaceProvider().getCompatibilityInterface();
            String databaseLogName = compatibilityInterface.getDatabaseLogName();

            return connectionHandler.getName() + " - " + CommonUtil.nvl(databaseLogName, "Log Output");
        } else {
            return connectionHandler.getName() + " - " + sourceFile.getName();
        }
    }

    public boolean matches(ConnectionHandler connectionHandler, VirtualFile sourceFile) {
        return getConnectionHandler() == connectionHandler && CommonUtil.safeEqual(this.sourceFile, sourceFile);
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
        return connectionHandlerRef.getConnectionId();
    }

    @Nullable
    public VirtualFile getSourceFile() {
        return sourceFile;
    }

    @NotNull
    @Override
    public ConnectionHandler getConnectionHandler() {
        return ConnectionHandlerRef.get(connectionHandlerRef);
    }

    @Override
    public PsiFile createPreviewFile() {
        return null;
    }

    public void write(String string, boolean addHeadline, boolean writeEmptyLines) {
        if (logOutputForm != null && ! logOutputForm.isDisposed()) {
            DatabaseLogOutputConsole console = logOutputForm.getConsole();
            if (addHeadline) {
                ConsoleView consoleView = console.getConsole();
                if (consoleView instanceof ConsoleViewImpl) {
                    ConsoleViewImpl consoleViewImpl = (ConsoleViewImpl) consoleView;
                    consoleViewImpl.requestScrollingToEnd();
                }
            }
            console.writeToConsole(string, addHeadline, writeEmptyLines);
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
        ScriptExecutionManager executionManager = ScriptExecutionManager.getInstance(getProject());
        executionManager.killProcess(sourceFile);
        sourceFile = null;
    }
}
