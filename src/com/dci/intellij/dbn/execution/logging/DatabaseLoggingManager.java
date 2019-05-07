package com.dci.intellij.dbn.execution.logging;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.notification.NotificationGroup;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

public class DatabaseLoggingManager extends AbstractProjectComponent {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private DatabaseLoggingManager(Project project) {
        super(project);
    }

    public static DatabaseLoggingManager getInstance(@NotNull Project project) {
        return Failsafe.getComponent(project, DatabaseLoggingManager.class);
    }

    /*********************************************************
     *                       Custom                          *
     *********************************************************/
    public boolean enableLogger(ConnectionHandler connectionHandler, DBNConnection connection) {
        DatabaseInterfaceProvider interfaceProvider = connectionHandler.getInterfaceProvider();
        if (DatabaseFeature.DATABASE_LOGGING.isSupported(connectionHandler)) {
            try {
                DatabaseMetadataInterface metadataInterface = interfaceProvider.getMetadataInterface();
                metadataInterface.enableLogger(connection);
                return true;
            } catch (SQLException e) {
                LOGGER.warn("Error enabling database logging: " + e.getMessage());
                DatabaseCompatibilityInterface compatibilityInterface = interfaceProvider.getCompatibilityInterface();
                String logName = getLogName(compatibilityInterface);
                sendWarningNotification(
                        NotificationGroup.LOGGING,
                        "Error enabling {0}: {1}", logName, e);
                return false;
            }
        }

        return false;
    }

    public void disableLogger(ConnectionHandler connectionHandler, @Nullable DBNConnection connection) {
        if (connection != null) {
            DatabaseInterfaceProvider interfaceProvider = connectionHandler.getInterfaceProvider();
            if (DatabaseFeature.DATABASE_LOGGING.isSupported(connectionHandler)) {
                try {
                    DatabaseMetadataInterface metadataInterface = interfaceProvider.getMetadataInterface();
                    metadataInterface.disableLogger(connection);
                } catch (SQLException e) {
                    LOGGER.warn("Error disabling database logging: " + e.getMessage());
                    DatabaseCompatibilityInterface compatibilityInterface = interfaceProvider.getCompatibilityInterface();
                    String logName = getLogName(compatibilityInterface);
                    sendWarningNotification(
                            NotificationGroup.LOGGING,
                            "Error disabling {0}: {1}", logName, e);
                }
            }
        }
    }

    public String readLoggerOutput(ConnectionHandler connectionHandler, DBNConnection connection) {
        DatabaseInterfaceProvider interfaceProvider = connectionHandler.getInterfaceProvider();
        DatabaseCompatibilityInterface compatibilityInterface = interfaceProvider.getCompatibilityInterface();
        try {
            DatabaseMetadataInterface metadataInterface = interfaceProvider.getMetadataInterface();
            return metadataInterface.readLoggerOutput(connection);
        } catch (SQLException e) {
            LOGGER.warn("Error reading database log output: " + e.getMessage());
            String logName = getLogName(compatibilityInterface);
            sendWarningNotification(
                    NotificationGroup.LOGGING,
                    "Error loading {0}: {1}", logName, e);
        }

        return null;
    }

    @Nullable
    private String getLogName(@Nullable DatabaseCompatibilityInterface compatibilityInterface) {
        String logName = compatibilityInterface == null ? null : compatibilityInterface.getDatabaseLogName();
        if (StringUtil.isEmpty(logName)) {
            logName = "database logging";
        }
        return logName;
    }

    public boolean supportsLogging(ConnectionHandler connectionHandler) {
        return DatabaseFeature.DATABASE_LOGGING.isSupported(connectionHandler);
    }


    /*********************************************************
     *                    ProjectComponent                   *
     *********************************************************/
    @Override
    @NotNull
    @NonNls
    public String getComponentName() {
        return "DBNavigator.Project.DatabaseLoggingManager";
    }
}
