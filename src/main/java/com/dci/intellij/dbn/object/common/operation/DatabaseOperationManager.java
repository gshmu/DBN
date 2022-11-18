package com.dci.intellij.dbn.object.common.operation;

import com.dci.intellij.dbn.common.component.Components;
import com.dci.intellij.dbn.common.component.ProjectComponentBase;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterfaceInvoker;
import com.dci.intellij.dbn.database.interfaces.DatabaseMetadataInterface;
import com.dci.intellij.dbn.database.interfaces.queue.InterfaceTaskDefinition;
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
        InterfaceTaskDefinition info = taskInfo("Enabling", constraint);
        DatabaseInterfaceInvoker.execute(info, conn -> {
            DatabaseMetadataInterface metadata = constraint.getMetadataInterface();
            metadata.enableConstraint(
                    constraint.getSchema().getName(),
                    constraint.getDataset().getName(),
                    constraint.getName(),
                    conn);
            constraint.getStatus().set(DBObjectStatus.ENABLED, true);
        });
    }

    public void disableConstraint(DBConstraint constraint) throws SQLException {
        InterfaceTaskDefinition info = taskInfo("Disabling", constraint);
        DatabaseInterfaceInvoker.execute(info, conn -> {
            DatabaseMetadataInterface metadata = constraint.getMetadataInterface();
            metadata.disableConstraint(
                    constraint.getSchema().getName(),
                    constraint.getDataset().getName(),
                    constraint.getName(),
                    conn);
            constraint.getStatus().set(DBObjectStatus.ENABLED, true);
        });
    }

    public void enableTrigger(DBTrigger trigger) throws SQLException {
        InterfaceTaskDefinition info = taskInfo("Enabling", trigger);
        DatabaseInterfaceInvoker.execute(info, conn -> {
            DatabaseMetadataInterface metadata = trigger.getMetadataInterface();
            metadata.enableTrigger(
                    trigger.getSchema().getName(),
                    trigger.getName(),
                    conn);
            trigger.getStatus().set(DBObjectStatus.ENABLED, true);
        });
    }

    public void disableTrigger(DBTrigger trigger) throws SQLException {
        InterfaceTaskDefinition info = taskInfo("Enabling", trigger);
        DatabaseInterfaceInvoker.execute(info, conn -> {
            DatabaseMetadataInterface metadata = trigger.getMetadataInterface();
            metadata.disableTrigger(
                    trigger.getSchema().getName(),
                    trigger.getName(),
                    conn);
            trigger.getStatus().set(DBObjectStatus.ENABLED, true);
        });
    }

    @NotNull
    private static InterfaceTaskDefinition taskInfo(String action, DBObject object) {
        InterfaceTaskDefinition taskDefinition = InterfaceTaskDefinition.create(HIGHEST,
                action + " " + object.getTypeName(),
                action + " " + object.getQualifiedNameWithType(),
                object.getConnection().getInterfaceContext());
        return taskDefinition;
    }
}
