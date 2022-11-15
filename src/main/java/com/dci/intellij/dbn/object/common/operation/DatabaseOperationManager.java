package com.dci.intellij.dbn.object.common.operation;

import com.dci.intellij.dbn.common.component.Components;
import com.dci.intellij.dbn.common.component.ProjectComponentBase;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterfaceInvoker;
import com.dci.intellij.dbn.database.interfaces.DatabaseMetadataInterface;
import com.dci.intellij.dbn.object.DBConstraint;
import com.dci.intellij.dbn.object.DBTrigger;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class DatabaseOperationManager extends ProjectComponentBase {

    public static final String COMPONENT_NAME = "DBNavigator.Project.OperationManager";

    private DatabaseOperationManager(Project project) {
        super(project, COMPONENT_NAME);
    }

    public static DatabaseOperationManager getInstance(@NotNull Project project) {
        return Components.projectService(project, DatabaseOperationManager.class);
    }

    public void enableConstraint(DBConstraint constraint) throws SQLException {
        ConnectionHandler connection = constraint.getConnection();
        DatabaseInterfaceInvoker.run(connection.context(), conn -> {
            DatabaseMetadataInterface metadata = connection.getMetadataInterface();
            metadata.enableConstraint(
                    constraint.getSchema().getName(),
                    constraint.getDataset().getName(),
                    constraint.getName(),
                    conn);
            constraint.getStatus().set(DBObjectStatus.ENABLED, true);
        });
    }

    public void disableConstraint(DBConstraint constraint) throws SQLException {
        ConnectionHandler connection = constraint.getConnection();
        DatabaseInterfaceInvoker.run(connection.context(), conn -> {
            DatabaseMetadataInterface metadata = connection.getMetadataInterface();
            metadata.disableConstraint(
                    constraint.getSchema().getName(),
                    constraint.getDataset().getName(),
                    constraint.getName(),
                    conn);
            constraint.getStatus().set(DBObjectStatus.ENABLED, true);
        });
    }

    public void enableTrigger(DBTrigger trigger) throws SQLException {
        ConnectionHandler connection = trigger.getConnection();
        DatabaseInterfaceInvoker.run(connection.context(), conn -> {
            DatabaseMetadataInterface metadata = connection.getMetadataInterface();
            metadata.enableTrigger(
                    trigger.getSchema().getName(),
                    trigger.getName(),
                    conn);
            trigger.getStatus().set(DBObjectStatus.ENABLED, true);
        });
    }

    public void disableTrigger(DBTrigger trigger) throws SQLException {
        ConnectionHandler connection = trigger.getConnection();
        DatabaseInterfaceInvoker.run(connection.context(), conn -> {
            DatabaseMetadataInterface metadata = connection.getMetadataInterface();
            metadata.disableTrigger(
                    trigger.getSchema().getName(),
                    trigger.getName(),
                    conn);
            trigger.getStatus().set(DBObjectStatus.ENABLED, true);
        });
    }
}
