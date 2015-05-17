package com.dci.intellij.dbn.connection.transaction.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.action.AbstractConnectionToggleAction;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;

public class ToggleDatabaseLoggingAction extends AbstractConnectionToggleAction {

    public ToggleDatabaseLoggingAction(ConnectionHandler connectionHandler) {
        super("Database Logging", connectionHandler);
    }

    @Override
    public boolean isSelected(AnActionEvent e) {
        return getConnectionHandler().isLoggingEnabled();
    }

    @Override
    public void setSelected(AnActionEvent e, boolean state) {
        getConnectionHandler().setLoggingEnabled(state);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        ConnectionHandler connectionHandler = getConnectionHandler();
        DatabaseCompatibilityInterface compatibilityInterface = connectionHandler.getInterfaceProvider().getCompatibilityInterface();
        boolean supportsLogging = DatabaseFeature.DATABASE_LOGGING.isSupported(connectionHandler);
        Presentation presentation = e.getPresentation();
        presentation.setVisible(supportsLogging);
        String databaseLogName = compatibilityInterface.getDatabaseLogName();
        if (StringUtil.isNotEmpty(databaseLogName)) {
            presentation.setText("Database Logging (" + databaseLogName + ")");
        }
    }
}
