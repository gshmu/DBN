package com.dci.intellij.dbn.debugger.frame;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.database.common.debug.VariableInfo;
import com.dci.intellij.dbn.debugger.DBProgramDebugProcess;
import com.intellij.xdebugger.frame.XValue;
import com.intellij.xdebugger.frame.XValueModifier;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import java.sql.SQLException;

public class DBProgramDebugValue extends XValue implements Comparable<DBProgramDebugValue>{
    private DBProgramDebugValueModifier modifier;
    private DBProgramDebugProcess debugProcess;
    private String textPresentation;
    private String variableName;
    private String errorMessage;
    private Icon icon;
    private int frameIndex;

    public DBProgramDebugValue(DBProgramDebugProcess debugProcess, String variableName, Icon icon, int frameIndex) {
        this.variableName = variableName;
        this.debugProcess = debugProcess;
        this.icon = icon == null ? Icons.DBO_VARIABLE : icon;
        this.frameIndex = frameIndex;
        try {
            VariableInfo variableInfo = debugProcess.getDebuggerInterface().getVariableInfo(
                    variableName.toUpperCase(), frameIndex,
                    debugProcess.getDebugConnection());
            textPresentation = variableInfo.getValue();
            errorMessage = variableInfo.getError();
            
            if (textPresentation == null) {
                textPresentation = "null";
            } else {
                if (!StringUtil.isNumber(textPresentation)) {
                    textPresentation = '"' + textPresentation + '"';
                }
            }

            if (errorMessage != null) {
                errorMessage = errorMessage.toLowerCase();
            }
        } catch (SQLException e) {
            textPresentation = "";
            errorMessage = e.getMessage();
        }
    }

    public DBProgramDebugProcess getDebugProcess() {
        return debugProcess;
    }

    public String getVariableName() {
        return variableName;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public void computePresentation(@NotNull XValueNode node, @NotNull XValuePlace place) {
        node.setPresentation(icon, errorMessage, textPresentation, false);
    }

    @Override
    public XValueModifier getModifier() {
        if (modifier == null) modifier = new DBProgramDebugValueModifier(this);
        return modifier;
    }

    public int compareTo(@NotNull DBProgramDebugValue remote) {
        return variableName.compareTo(remote.variableName);
    }
}
