package com.dci.intellij.dbn.execution.logging;

import com.dci.intellij.dbn.common.component.ProjectComponentBase;
import com.dci.intellij.dbn.common.notification.NotificationGroup;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.interfaces.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.interfaces.DatabaseMetadataInterface;
import com.intellij.openapi.project.Project;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

import static com.dci.intellij.dbn.common.component.Components.projectService;
import static com.dci.intellij.dbn.diagnostics.Diagnostics.conditionallyLog;

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
        if (!DatabaseFeature.DATABASE_LOGGING.isSupported(connection)) return false;

        try {
            DatabaseMetadataInterface metadata = connection.getMetadataInterface();
            metadata.enableLogger(conn);
            return true;
        } catch (SQLException e) {
            conditionallyLog(e);
            log.warn("Error enabling database logging: " + e.getMessage());
            String logName = getLogName(connection);
            sendWarningNotification(
                    NotificationGroup.LOGGING,
                    "Error enabling {0}: {1}", logName, e);
            return false;
        }
    }

    public void disableLogger(ConnectionHandler connection, @Nullable DBNConnection conn) {
        if (conn == null)  return;
        if (!DatabaseFeature.DATABASE_LOGGING.isSupported(connection)) return;

        try {
            DatabaseMetadataInterface metadata = connection.getMetadataInterface();
            metadata.disableLogger(conn);
        } catch (SQLException e) {
            conditionallyLog(e);
            log.warn("Error disabling database logging: " + e.getMessage());
            String logName = getLogName(connection);
            sendWarningNotification(
                    NotificationGroup.LOGGING,
                    "Error disabling {0}: {1}", logName, e);
        }
    }

    public String readLoggerOutput(ConnectionHandler connection, DBNConnection conn) {
        try {
            DatabaseMetadataInterface metadata = connection.getMetadataInterface();
            return metadata.readLoggerOutput(conn);
        } catch (SQLException e) {
            conditionallyLog(e);
            log.warn("Error reading database log output: " + e.getMessage());
            String logName = getLogName(connection);
            sendWarningNotification(
                    NotificationGroup.LOGGING,
                    "Error loading {0}: {1}", logName, e);
        }

        return null;
    }

    @NotNull
    private String getLogName(ConnectionHandler connection) {
        DatabaseCompatibilityInterface compatibility = connection.getCompatibilityInterface();
        String logName = compatibility.getDatabaseLogName();
        if (Strings.isEmpty(logName)) {
            logName = "database logging";
        }
        return logName;
    }

    public boolean supportsLogging(ConnectionHandler connection) {
        return DatabaseFeature.DATABASE_LOGGING.isSupported(connection);
    }

}
