package com.dci.intellij.dbn.execution.logging.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.StringUtil;
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
import org.jetbrains.annotations.Nullable;

import static com.dci.intellij.dbn.common.util.ActionUtil.getProject;
import static com.dci.intellij.dbn.common.util.ActionUtil.getVirtualFile;

public class ToggleDatabaseLoggingEditorAction extends ToggleAction implements DumbAware {

    public ToggleDatabaseLoggingEditorAction() {
        super("Enable / Disable Database Logging", null, Icons.EXEC_LOG_OUTPUT_CONSOLE);
    }

    public boolean isSelected(AnActionEvent e) {
        ConnectionHandler activeConnection = getConnectionHandler(e);
        return activeConnection != null && activeConnection.isLoggingEnabled();
    }

    @Nullable
    private static ConnectionHandler getConnectionHandler(AnActionEvent e) {
        Project project = getProject(e);
        VirtualFile virtualFile = getVirtualFile(e);
        if (project != null && virtualFile != null) {
            FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
            ConnectionHandler activeConnection = connectionMappingManager.getConnectionHandler(virtualFile);
            if (activeConnection != null && !activeConnection.isVirtual() && !activeConnection.isDisposed()) {
                return activeConnection ;
            }

        }
        return null;
    }

    public void setSelected(AnActionEvent e, boolean selected) {
        ConnectionHandler activeConnection = getConnectionHandler(e);
        if (activeConnection != null) activeConnection.setLoggingEnabled(selected);
    }

    public void update(AnActionEvent e) {
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
                if (StringUtil.isNotEmpty(databaseLogName)) {
                    name = name + " (" + databaseLogName + ")";
                }
            }
        }
        presentation.setText(name);
        presentation.setVisible(visible);
    }

    public static boolean isVisible(AnActionEvent e) {
        VirtualFile virtualFile = getVirtualFile(e);
        return !DatabaseDebuggerManager.isDebugConsole(virtualFile);
    }
}