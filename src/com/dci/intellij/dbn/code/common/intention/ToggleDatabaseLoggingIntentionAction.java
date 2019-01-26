package com.dci.intellij.dbn.code.common.intention;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.language.common.PsiFileRef;
import com.intellij.codeInsight.intention.LowPriorityAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ToggleDatabaseLoggingIntentionAction extends GenericIntentionAction implements LowPriorityAction {
    private PsiFileRef lastChecked;
    @Override
    @NotNull
    public String getText() {
        ConnectionHandler connectionHandler = getLastCheckedConnection();
        if (connectionHandler != null) {
            String databaseLogName = connectionHandler.getInterfaceProvider().getCompatibilityInterface().getDatabaseLogName();
            if (StringUtil.isEmpty(databaseLogName)) {
                return connectionHandler.isLoggingEnabled() ? "Disable database logging" : "Enable database logging";
            } else {
                return (connectionHandler.isLoggingEnabled() ? "Disable logging (" : "Enable logging (") + databaseLogName + ')';
            }
        }

        return "Database logging";
    }

    @Override
    public Icon getIcon(int flags) {
        ConnectionHandler connectionHandler = getLastCheckedConnection();
        if (connectionHandler != null) {
            return connectionHandler.isLoggingEnabled() ? Icons.EXEC_LOG_OUTPUT_DISABLE : Icons.EXEC_LOG_OUTPUT_ENABLE;
        }
        return Icons.EXEC_LOG_OUTPUT_CONSOLE;
    }

    @Override
    @NotNull
    public String getFamilyName() {
        return IntentionActionGroups.STATEMENT_EXECUTION;
    }

    ConnectionHandler getLastCheckedConnection() {
        if (lastChecked != null && lastChecked.get() != null) {
            ConnectionHandler connectionHandler = getConnectionHandler(lastChecked.get());
            if (supportsLogging(connectionHandler)) {
                return connectionHandler;
            }
        }
        return null;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        VirtualFile virtualFile = psiFile.getVirtualFile();
        if (DatabaseDebuggerManager.isDebugConsole(virtualFile)) {
            return false;
        }

        lastChecked = PsiFileRef.from(psiFile);
        ConnectionHandler connectionHandler = getConnectionHandler(psiFile);
        return supportsLogging(connectionHandler);
    }

    private static boolean supportsLogging(ConnectionHandler connectionHandler) {
        return connectionHandler != null &&
                !connectionHandler.isDisposed() &&
                !connectionHandler.isVirtual() &&
                DatabaseFeature.DATABASE_LOGGING.isSupported(connectionHandler);
    }

    @Override
    public void invoke(@NotNull final Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        ConnectionHandler connectionHandler = getConnectionHandler(psiFile);
        if (DatabaseFeature.DATABASE_LOGGING.isSupported(connectionHandler)) {
            connectionHandler.setLoggingEnabled(!connectionHandler.isLoggingEnabled());
        }
    }


    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @NotNull
    @Override
    public Priority getPriority() {
        return Priority.LOW;
    }
}
