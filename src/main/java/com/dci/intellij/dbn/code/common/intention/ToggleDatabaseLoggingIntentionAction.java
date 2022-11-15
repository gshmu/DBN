package com.dci.intellij.dbn.code.common.intention;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.thread.Read;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.interfaces.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.PsiFileRef;
import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.codeInsight.intention.LowPriorityAction;
import com.intellij.injected.editor.VirtualFileWindow;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ToggleDatabaseLoggingIntentionAction extends GenericIntentionAction implements LowPriorityAction {
    private PsiFileRef<?> lastChecked;

    @Override
    @NotNull
    public String getText() {
        ConnectionHandler connection = getLastCheckedConnection();
        if (Failsafe.check(connection)) {
            DatabaseCompatibilityInterface compatibility = connection.getCompatibilityInterface();
            String databaseLogName = compatibility.getDatabaseLogName();
            boolean loggingEnabled = connection.isLoggingEnabled();
            if (Strings.isEmpty(databaseLogName)) {
                return loggingEnabled ? "Disable database logging" : "Enable database logging";
            } else {
                return (loggingEnabled ? "Disable logging (" : "Enable logging (") + databaseLogName + ')';
            }
        }

        return "Toggle database logging";
    }

    @Override
    public Icon getIcon(int flags) {
        ConnectionHandler connection = getLastCheckedConnection();
        if (connection != null) {
            return connection.isLoggingEnabled() ? Icons.EXEC_LOG_OUTPUT_DISABLE : Icons.EXEC_LOG_OUTPUT_ENABLE;
        }
        return Icons.EXEC_LOG_OUTPUT_CONSOLE;
    }


    ConnectionHandler getLastCheckedConnection() {
        PsiFile psiFile = Read.conditional(() -> PsiFileRef.from(lastChecked));
        if (psiFile != null) {
            ConnectionHandler connection = getConnection(psiFile);
            if (supportsLogging(connection)) {
                return connection;
            }
        }
        return null;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        if (psiFile instanceof DBLanguagePsiFile) {
            VirtualFile virtualFile = psiFile.getVirtualFile();
            if (virtualFile instanceof DBSourceCodeVirtualFile ||
                    virtualFile instanceof VirtualFileWindow ||
                    DatabaseDebuggerManager.isDebugConsole(virtualFile)) {
                return false;
            }

            lastChecked = PsiFileRef.of(psiFile);
            ConnectionHandler connection = getConnection(psiFile);
            return supportsLogging(connection);
        }
        return false;
    }

    private static boolean supportsLogging(ConnectionHandler connection) {
        return Failsafe.check(connection) &&
                !connection.isVirtual() &&
                DatabaseFeature.DATABASE_LOGGING.isSupported(connection);
    }

    @Override
    public void invoke(@NotNull final Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        ConnectionHandler connection = getConnection(psiFile);
        if (DatabaseFeature.DATABASE_LOGGING.isSupported(connection)) {
            connection.setLoggingEnabled(!connection.isLoggingEnabled());
        }
    }


    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    protected Integer getGroupPriority() {
        return 0;
    }
}
