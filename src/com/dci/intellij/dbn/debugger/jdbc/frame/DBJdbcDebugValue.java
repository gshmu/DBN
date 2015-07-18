package com.dci.intellij.dbn.debugger.jdbc.frame;

import javax.swing.Icon;
import java.sql.SQLException;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.thread.SimpleBackgroundTask;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.database.common.debug.VariableInfo;
import com.dci.intellij.dbn.debugger.common.frame.DBDebugValue;
import com.dci.intellij.dbn.debugger.jdbc.DBJdbcDebugProcess;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XValueChildrenList;
import com.intellij.xdebugger.frame.XValueModifier;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;

public class DBJdbcDebugValue extends DBDebugValue<DBJdbcDebugStackFrame>{
    private DBJdbcDebugValueModifier modifier;

    public DBJdbcDebugValue(DBJdbcDebugStackFrame stackFrame, DBJdbcDebugValue parentValue, String variableName, @Nullable Set<String> childVariableNames, Icon icon) {
        super(stackFrame, variableName, childVariableNames, parentValue, icon);
    }

    @Override
    public void computePresentation(@NotNull final XValueNode node, @NotNull XValuePlace place) {
        // enabling this will show always variables as changed
        //node.setPresentation(icon, null, "", childVariableNames != null);
        new SimpleBackgroundTask("load variable value") {
            @Override
            protected void execute() {
                try {
                    DBJdbcDebugProcess debugProcess = getDebugProcess();
                    String variableName = getVariableName();
                    DBDebugValue parentValue = getParentValue();
                    String databaseVariableName = parentValue == null ? variableName : parentValue.getVariableName() + "." + variableName;
                    VariableInfo variableInfo = debugProcess.getDebuggerInterface().getVariableInfo(
                            databaseVariableName.toUpperCase(),
                            getStackFrame().getFrameIndex(),
                            debugProcess.getDebugConnection());
                    value = variableInfo.getValue();
                    errorMessage = variableInfo.getError();
                    if (childVariableNames != null) {
                        errorMessage = null;
                    }

                    if (value == null) {
                        value = childVariableNames != null ? "" : "null";
                    } else {
                        if (!StringUtil.isNumber(value)) {
                            value = '\'' + value + '\'';
                        }
                    }

                    if (errorMessage != null) {
                        errorMessage = errorMessage.toLowerCase();
                        value = "";
                    }
                    if (childVariableNames != null) {
                        errorMessage = "record";
                    }
                } catch (SQLException e) {
                    value = "";
                    errorMessage = e.getMessage();
                } finally {
                    node.setPresentation(icon, errorMessage, CommonUtil.nvl(value, "null"), childVariableNames != null);
                }

            }
        }.start();
    }

    @Override
    public DBJdbcDebugProcess getDebugProcess() {
        return (DBJdbcDebugProcess) super.getDebugProcess();
    }

    @Override
    public XValueModifier getModifier() {
        if (modifier == null) modifier = new DBJdbcDebugValueModifier(this);
        return modifier;
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        if (childVariableNames != null) {
            for (String childVariableName : childVariableNames) {
                childVariableName = childVariableName.substring(getVariableName().length() + 1);
                XValueChildrenList debugValueChildren = new XValueChildrenList();
                DBJdbcDebugValue value = new DBJdbcDebugValue(getStackFrame(), this, childVariableName, null, null);
                debugValueChildren.add(value);
                node.addChildren(debugValueChildren, true);
            }
        } else {
            super.computeChildren(node);
        }

    }
}
