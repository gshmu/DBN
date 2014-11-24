package com.dci.intellij.dbn.execution.logging;

import java.sql.Connection;
import java.sql.SQLException;

import com.dci.intellij.dbn.common.util.StringUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.notification.NotificationUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

public class DatabaseLoggingManager extends AbstractProjectComponent {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private DatabaseLoggingManager(Project project) {
        super(project);
    }

    public static DatabaseLoggingManager getInstance(Project project) {
        return project.getComponent(DatabaseLoggingManager.class);
    }

    @Override
    public void disposeComponent() {
        super.disposeComponent();
    }

    /*********************************************************
     *                       Custom                          *
     *********************************************************/
    public boolean enableLogger(ConnectionHandler connectionHandler, Connection connection) {
        DatabaseInterfaceProvider interfaceProvider = connectionHandler.getInterfaceProvider();
        DatabaseCompatibilityInterface compatibilityInterface = interfaceProvider.getCompatibilityInterface();
        if (compatibilityInterface.supportsFeature(DatabaseFeature.DATABASE_LOGGING)) {
            try {
                DatabaseMetadataInterface metadataInterface = interfaceProvider.getMetadataInterface();
                metadataInterface.enableLogger(connection);
                return true;
            } catch (SQLException e) {
                LOGGER.error("Error enabling database logging", e);
                String logName = getLogName(compatibilityInterface);
                NotificationUtil.sendWarningNotification(connectionHandler.getProject(), "Database Logging", "Error enabling " + logName + ": " + e.getMessage());
                return false;
            }
        }

        return false;
    }

    public void disableLogger(ConnectionHandler connectionHandler, @Nullable Connection connection) {
        if (connection != null) {
            DatabaseInterfaceProvider interfaceProvider = connectionHandler.getInterfaceProvider();
            DatabaseCompatibilityInterface compatibilityInterface = interfaceProvider.getCompatibilityInterface();
            if (compatibilityInterface.supportsFeature(DatabaseFeature.DATABASE_LOGGING)) {
                try {
                    DatabaseMetadataInterface metadataInterface = interfaceProvider.getMetadataInterface();
                    metadataInterface.disableLogger(connection);
                } catch (SQLException e) {
                    LOGGER.error("Error disabling database logging", e);
                    String logName = getLogName(compatibilityInterface);
                    NotificationUtil.sendWarningNotification(connectionHandler.getProject(), "Database Logging", "Error disabling " + logName + ": " + e.getMessage());
                }
            }
        }
    }

    public String readLoggerOutput(ConnectionHandler connectionHandler, Connection connection) {
        DatabaseInterfaceProvider interfaceProvider = connectionHandler.getInterfaceProvider();
        DatabaseCompatibilityInterface compatibilityInterface = interfaceProvider.getCompatibilityInterface();
        try {
            DatabaseMetadataInterface metadataInterface = interfaceProvider.getMetadataInterface();
            return metadataInterface.readLoggerOutput(connection);
        } catch (SQLException e) {
            LOGGER.error("Error disabling database logging", e);
            String logName = getLogName(compatibilityInterface);
            NotificationUtil.sendWarningNotification(connectionHandler.getProject(), "Database Logging", "Error loading " + logName + " : " + e.getMessage());
        }

        return null;
    }

    private String getLogName(DatabaseCompatibilityInterface compatibilityInterface) {
        String logName = compatibilityInterface.getDatabaseLogName();
        if (StringUtil.isEmpty(logName)) {
            logName = "database logging";
        }
        return logName;
    }

    public boolean supportsLogging(ConnectionHandler connectionHandler) {
        DatabaseCompatibilityInterface compatibilityInterface = connectionHandler.getInterfaceProvider().getCompatibilityInterface();
        return compatibilityInterface.supportsFeature(DatabaseFeature.DATABASE_LOGGING);
    }


    /*********************************************************
     *                    ProjectComponent                   *
     *********************************************************/
    @NotNull
    @NonNls
    public String getComponentName() {
        return "DBNavigator.Project.DatabaseLoggingManager";
    }
}
