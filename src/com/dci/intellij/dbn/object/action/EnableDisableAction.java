package com.dci.intellij.dbn.object.action;

import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.operation.DBOperationNotSupportedException;
import com.dci.intellij.dbn.object.common.operation.DBOperationType;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

import static com.dci.intellij.dbn.common.util.ActionUtil.ensureProject;

public class EnableDisableAction extends DumbAwareAction {
    private DBSchemaObject object;

    public EnableDisableAction(DBSchemaObject object) {
        super("Enable/Disable");
        this.object = object;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = ensureProject(e);
        boolean enabled = object.getStatus().is(DBObjectStatus.ENABLED);
        String objectName = object.getQualifiedNameWithType();
        Progress.prompt(project, enabled ? "Disabling " : "Enabling " + objectName, false,
                (progress) -> {
                    try {
                        DBOperationType operationType = enabled ? DBOperationType.DISABLE : DBOperationType.ENABLE;
                        object.getOperationExecutor().executeOperation(operationType);
                    } catch (SQLException e1) {

                        String message = "Error " + (!enabled ? "enabling " : "disabling ") + objectName;
                        MessageUtil.showErrorDialog(project, message, e1);
                    } catch (DBOperationNotSupportedException e1) {
                        MessageUtil.showErrorDialog(project, e1.getMessage());
                    }
                });
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        boolean enabled = object.getStatus().is(DBObjectStatus.ENABLED);
        e.getPresentation().setText(!enabled? "Enable" : "Disable");
    }
}