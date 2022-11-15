package com.dci.intellij.dbn.connection.transaction.action;

import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.action.AbstractConnectionToggleAction;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.interfaces.DatabaseCompatibilityInterface;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;

public class DatabaseLoggingToggleAction extends AbstractConnectionToggleAction {

    public DatabaseLoggingToggleAction(ConnectionHandler connection) {
        super("Database Logging", connection);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return getConnection().isLoggingEnabled();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        getConnection().setLoggingEnabled(state);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        ConnectionHandler connection = getConnection();
        DatabaseCompatibilityInterface compatibility = connection.getCompatibilityInterface();
        boolean supportsLogging = DatabaseFeature.DATABASE_LOGGING.isSupported(connection);
        Presentation presentation = e.getPresentation();
        presentation.setVisible(supportsLogging);
        String databaseLogName = compatibility.getDatabaseLogName();
        if (Strings.isNotEmpty(databaseLogName)) {
            presentation.setText("Database Logging (" + databaseLogName + ")");
        }
    }
}
