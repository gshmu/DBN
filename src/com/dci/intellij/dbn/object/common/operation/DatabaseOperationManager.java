package com.dci.intellij.dbn.object.common.operation;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseInterface;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.object.DBConstraint;
import com.dci.intellij.dbn.object.DBTrigger;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class DatabaseOperationManager extends AbstractProjectComponent {
    private DatabaseOperationManager(Project project) {
        super(project);
    }

    public static DatabaseOperationManager getInstance(@NotNull Project project) {
        return Failsafe.getComponent(project, DatabaseOperationManager.class);
    }

    public void enableConstraint(DBConstraint constraint) throws SQLException {
        ConnectionHandler connectionHandler = constraint.getConnectionHandler();
        DatabaseInterface.run(true,
                connectionHandler,
                (provider, connection) -> {
                    DatabaseMetadataInterface metadataInterface = provider.getMetadataInterface();
                    metadataInterface.enableConstraint(
                            constraint.getSchema().getName(),
                            constraint.getDataset().getName(),
                            constraint.getName(),
                            connection);
                    constraint.getStatus().set(DBObjectStatus.ENABLED, true);
                });
    }

    public void disableConstraint(DBConstraint constraint) throws SQLException {
        ConnectionHandler connectionHandler = constraint.getConnectionHandler();
        DatabaseInterface.run(true,
                connectionHandler,
                (provider, connection) -> {
                    DatabaseMetadataInterface metadataInterface = provider.getMetadataInterface();
                    metadataInterface.disableConstraint(
                            constraint.getSchema().getName(),
                            constraint.getDataset().getName(),
                            constraint.getName(),
                            connection);
                    constraint.getStatus().set(DBObjectStatus.ENABLED, true);
                });
    }

    public void enableTrigger(DBTrigger trigger) throws SQLException {
        ConnectionHandler connectionHandler = trigger.getConnectionHandler();
        DatabaseInterface.run(true,
                connectionHandler,
                (provider, connection) -> {
                    DatabaseMetadataInterface metadataInterface = provider.getMetadataInterface();
                    metadataInterface.enableTrigger(
                            trigger.getSchema().getName(),
                            trigger.getName(),
                            connection);
                    trigger.getStatus().set(DBObjectStatus.ENABLED, true);
                });
    }

    public void disableTrigger(DBTrigger trigger) throws SQLException {
        ConnectionHandler connectionHandler = trigger.getConnectionHandler();
        DatabaseInterface.run(true,
                connectionHandler,
                (provider, connection) -> {
                    DatabaseMetadataInterface metadataInterface = provider.getMetadataInterface();
                    metadataInterface.disableTrigger(
                            trigger.getSchema().getName(),
                            trigger.getName(),
                            connection);
                    trigger.getStatus().set(DBObjectStatus.ENABLED, true);
                });
    }

    /***************************************
     *            ProjectComponent         *
     ***************************************/
    @Override
    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.OperationManager";
    }
}
