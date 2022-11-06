package com.dci.intellij.dbn.execution.logging;

import com.dci.intellij.dbn.common.component.ProjectComponentBase;
import com.dci.intellij.dbn.common.notification.NotificationGroup;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.intellij.openapi.project.Project;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

import static com.dci.intellij.dbn.common.component.Components.projectService;

@Slf4j
public class DatabaseLoggingManager extends ProjectComponentBase {

    public static final String COMPONENT_NAME = "DBNavigator.Project.DatabaseLoggingManager";

    private DatabaseLoggingManager(Project project) {
        super(project, COMPONENT_NAME);
    }

    public static DatabaseLoggingManager getInstance(@NotNull Project project) {
        return projectService(project, DatabaseLoggingManager.class);
    }

    /*********************************************************
     *                       Custom                          *
     *********************************************************/
    public boolean enableLogger(ConnectionHandler connection, DBNConnection conn) {
        DatabaseInterfaceProvider interfaceProvider = connection.getInterfaceProvider();
        if (DatabaseFeature.DATABASE_LOGGING.isSupported(connection)) {
            try {
                DatabaseMetadataInterface metadataInterface = interfaceProvider.getMetadataInterface();
                metadataInterface.enableLogger(conn);
                return true;
            } catch (SQLException e) {
                log.warn("Error enabling database logging: " + e.getMessage());
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

    public void disableLogger(ConnectionHandler connection, @Nullable DBNConnection conn) {
        if (conn != null) {
            DatabaseInterfaceProvider interfaceProvider = connection.getInterfaceProvider();
            if (DatabaseFeature.DATABASE_LOGGING.isSupported(connection)) {
                try {
                    DatabaseMetadataInterface metadataInterface = interfaceProvider.getMetadataInterface();
                    metadataInterface.disableLogger(conn);
                } catch (SQLException e) {
                    log.warn("Error disabling database logging: " + e.getMessage());
                    DatabaseCompatibilityInterface compatibilityInterface = interfaceProvider.getCompatibilityInterface();
                    String logName = getLogName(compatibilityInterface);
                    sendWarningNotification(
                            NotificationGroup.LOGGING,
                            "Error disabling {0}: {1}", logName, e);
                }
            }
        }
    }

    public String readLoggerOutput(ConnectionHandler connection, DBNConnection conn) {
        DatabaseInterfaceProvider interfaceProvider = connection.getInterfaceProvider();
        DatabaseCompatibilityInterface compatibilityInterface = interfaceProvider.getCompatibilityInterface();
        try {
            DatabaseMetadataInterface metadataInterface = interfaceProvider.getMetadataInterface();
            return metadataInterface.readLoggerOutput(conn);
        } catch (SQLException e) {
            log.warn("Error reading database log output: " + e.getMessage());
            String logName = getLogName(compatibilityInterface);
            sendWarningNotification(
                    NotificationGroup.LOGGING,
                    "Error loading {0}: {1}", logName, e);
        }

        return null;
    }

    @NotNull
    private String getLogName(@Nullable DatabaseCompatibilityInterface compatibilityInterface) {
        String logName = compatibilityInterface == null ? null : compatibilityInterface.getDatabaseLogName();
        if (Strings.isEmpty(logName)) {
            logName = "database logging";
        }
        return logName;
    }

    public boolean supportsLogging(ConnectionHandler connection) {
        return DatabaseFeature.DATABASE_LOGGING.isSupported(connection);
    }

}
