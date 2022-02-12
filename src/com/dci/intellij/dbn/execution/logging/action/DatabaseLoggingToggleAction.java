package com.dci.intellij.dbn.execution.logging.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.Lookup;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
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
        ConnectionHandler activeConnection = getConnectionHandler(e);
        return activeConnection != null && activeConnection.isLoggingEnabled();
    }

    @Nullable
    private static ConnectionHandler getConnectionHandler(AnActionEvent e) {
        Project project = Lookup.getProject(e);
        VirtualFile virtualFile = Lookup.getVirtualFile(e);
        if (project != null && virtualFile != null) {
            FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
            ConnectionHandler activeConnection = connectionMappingManager.getConnection(virtualFile);
            if (Failsafe.check(activeConnection) && !activeConnection.isVirtual()) {
                return activeConnection ;
            }

        }
        return null;
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean selected) {
        ConnectionHandler activeConnection = getConnectionHandler(e);
        if (activeConnection != null) activeConnection.setLoggingEnabled(selected);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        ConnectionHandler activeConnection = getConnectionHandler(e);
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
        VirtualFile virtualFile = Lookup.getVirtualFile(e);
        return !DatabaseDebuggerManager.isDebugConsole(virtualFile);
    }
}