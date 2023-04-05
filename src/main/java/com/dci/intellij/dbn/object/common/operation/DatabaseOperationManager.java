package com.dci.intellij.dbn.object.common.operation;

import com.dci.intellij.dbn.common.component.Components;
import com.dci.intellij.dbn.common.component.ProjectComponentBase;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterfaceInvoker;
import com.dci.intellij.dbn.database.interfaces.DatabaseMetadataInterface;
import com.dci.intellij.dbn.object.DBConstraint;
import com.dci.intellij.dbn.object.DBTrigger;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

import static com.dci.intellij.dbn.common.Priority.HIGHEST;

public class DatabaseOperationManager extends ProjectComponentBase {

    public static final String COMPONENT_NAME = "DBNavigator.Project.OperationManager";

    private DatabaseOperationManager(Project project) {
        super(project, COMPONENT_NAME);
    }

    public static DatabaseOperationManager getInstance(@NotNull Project project) {
        return Components.projectService(project, DatabaseOperationManager.class);
    }

    public void enableConstraint(DBConstraint constraint) throws SQLException {
        DatabaseInterfaceInvoker.execute(HIGHEST,
                actionTitle("Enabling", constraint),
                actionText("Enabling", constraint),
                constraint.getProject(),
                constraint.getConnectionId(),
                conn -> {
                    DatabaseMetadataInterface metadata = constraint.getMetadataInterface();
                    metadata.enableConstraint(
                            constraint.getSchemaName(),
                            constraint.getDataset().getName(),
                            constraint.getName(),
                            conn);
                    constraint.getStatus().set(DBObjectStatus.ENABLED, true);
                });
    }

    public void disableConstraint(DBConstraint constraint) throws SQLException {
        DatabaseInterfaceInvoker.execute(HIGHEST,
                actionTitle("Disabling", constraint),
                actionText("Disabling", constraint),
                constraint.getProject(),
                constraint.getConnectionId(),
                conn -> {
                    DatabaseMetadataInterface metadata = constraint.getMetadataInterface();
                    metadata.disableConstraint(
                            constraint.getSchemaName(),
                            constraint.getDataset().getName(),
                            constraint.getName(),
                            conn);
                    constraint.getStatus().set(DBObjectStatus.ENABLED, false);
                });
    }

    public void enableTrigger(DBTrigger trigger) throws SQLException {
        DatabaseInterfaceInvoker.execute(HIGHEST,
                actionTitle("Enabling", trigger),
                actionText("Enabling", trigger),
                trigger.getProject(),
                trigger.getConnectionId(),
                conn -> {
                    DatabaseMetadataInterface metadata = trigger.getMetadataInterface();
                    metadata.enableTrigger(
                            trigger.getSchemaName(),
                            trigger.getName(),
                            conn);
                    trigger.getStatus().set(DBObjectStatus.ENABLED, true);
                });
    }

    public void disableTrigger(DBTrigger trigger) throws SQLException {
        DatabaseInterfaceInvoker.execute(HIGHEST,
                actionTitle("Disabling", trigger),
                actionText("Disabling", trigger),
                trigger.getProject(),
                trigger.getConnectionId(),
                conn -> {
                    DatabaseMetadataInterface metadata = trigger.getMetadataInterface();
                    metadata.disableTrigger(
                            trigger.getSchemaName(),
                            trigger.getName(),
                            conn);
                    trigger.getStatus().set(DBObjectStatus.ENABLED, false);
                });
    }

    @NotNull
    private static String actionTitle(String action, DBObject object) {
        return action + " " + object.getTypeName();
    }

    @NotNull
    private static String actionText(String action, DBObject object) {
        return action + " " + object.getQualifiedNameWithType();
    }
}
