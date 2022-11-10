package com.dci.intellij.dbn.execution.logging.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.Lookups;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionContextManager;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DatabaseLoggingToggleAction extends ToggleAction implements DumbAware {

    public DatabaseLoggingToggleAction() {
        super("Enable / Disable Database Logging", null, Icons.EXEC_LOG_OUTPUT_CONSOLE);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        ConnectionHandler activeConnection = getConnection(e);
        return activeConnection != null && activeConnection.isLoggingEnabled();
    }

    @Nullable
    private static ConnectionHandler getConnection(AnActionEvent e) {
        Project project = Lookups.getProject(e);
        VirtualFile virtualFile = Lookups.getVirtualFile(e);
        if (project != null && virtualFile != null) {
            FileConnectionContextManager contextManager = FileConnectionContextManager.getInstance(project);
            ConnectionHandler connection = contextManager.getConnection(virtualFile);
            if (Failsafe.check(connection) && !connection.isVirtual()) {
                return connection ;
            }

        }
        return null;
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean selected) {
        ConnectionHandler connection = getConnection(e);
        if (connection != null) connection.setLoggingEnabled(selected);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        ConnectionHandler activeConnection = getConnection(e);
        Presentation presentation = e.getPresentation();

        boolean visible = false;
        String name = "Database Logging";
        if (activeConnection != null) {
            boolean supportsLogging = DatabaseFeature.DATABASE_LOGGING.isSupported(activeConnection);
            if (supportsLogging && isVisible(e)) {
                visible = true;
                DatabaseCompatibilityInterface compatibilityInterface = activeConnection.getInterfaceProvider().getCompatibilityInterface();
                String databaseLogName = compatibilityInterface.getDatabaseLogName();
                if (Strings.isNotEmpty(databaseLogName)) {
                    name = name + " (" + databaseLogName + ")";
                }
            }
        }
        presentation.setText(name);
        presentation.setVisible(visible);
    }

    public static boolean isVisible(AnActionEvent e) {
        VirtualFile virtualFile = Lookups.getVirtualFile(e);
        return !DatabaseDebuggerManager.isDebugConsole(virtualFile);
    }
}