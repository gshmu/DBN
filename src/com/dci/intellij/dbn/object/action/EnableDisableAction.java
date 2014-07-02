package com.dci.intellij.dbn.object.action;

import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.operation.DBOperationNotSupportedException;
import com.dci.intellij.dbn.object.common.operation.DBOperationType;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import java.sql.SQLException;

public class EnableDisableAction extends AnAction {
    private DBSchemaObject object;

    public EnableDisableAction(DBSchemaObject object) {
        super("Enable/Disable");
        this.object = object;
    }

    public void actionPerformed(AnActionEvent e) {
        boolean enabled = object.getStatus().is(DBObjectStatus.ENABLED);
        try {
            DBOperationType operationType = enabled ? DBOperationType.DISABLE : DBOperationType.ENABLE;
            object.getOperationExecutor().executeOperation(operationType);
        } catch (SQLException e1) {
            String message = "Error " + (!enabled ? "enabling " : "disabling ") + object.getQualifiedNameWithType();
            MessageUtil.showErrorDialog(message, e1);
        } catch (DBOperationNotSupportedException e1) {
            MessageUtil.showErrorDialog(e1.getMessage());
        }
    }

    @Override
    public void update(AnActionEvent e) {
        boolean enabled = object.getStatus().is(DBObjectStatus.ENABLED);
        e.getPresentation().setText(!enabled? "Enable" : "Disable");
    }
}