package com.dci.intellij.dbn.debugger.jdwp.frame;

import com.dci.intellij.dbn.common.compatibility.Compatibility;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.debugger.jdwp.process.DBJdwpDebugProcess;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.intellij.xdebugger.XExpression;
import com.intellij.xdebugger.frame.XValueModifier;
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DBJdwpDebugValueModifier extends XValueModifier {
    private DBJdwpDebugValue value;

    DBJdwpDebugValueModifier(DBJdwpDebugValue value) {
        this.value = value;
    }

    @Override
    @Compatibility
    public void setValue(@NotNull String expression, @NotNull XModificationCallback callback) {
        setValue(new XExpressionImpl(expression, SQLLanguage.INSTANCE, null), callback);
    }

    @Override
    public void setValue(@NotNull XExpression expr, @NotNull XModificationCallback callback) {
        String expression = expr.getExpression();

        DBJdwpDebugProcess debugProcess = value.getDebugProcess();
        try {
            if (Strings.isNotEmpty(expression)) {
                while (expression.charAt(0) == '\'') {
                    expression = expression.substring(1);
                }

                while (expression.charAt(expression.length()-1) == '\'') {
                    expression = expression.substring(0, expression.length() -1);
                }
            }
/*
            BasicOperationInfo operationInfo = debugProcess.getDebuggerInterface().setVariableValue(
                    value.getVariableName(),
                    0,
                    expression,
                    debugProcess.getDebugConnection());

            if (operationInfo.getError() != null) {
                callback.errorOccurred("Could not change value. " + operationInfo.getError());
            } else {
                callback.valueModified();
            }
*/
        } catch (Exception e) {
            callback.errorOccurred(e.getMessage());
        }
    }

    @Nullable
    @Override
    public String getInitialValueEditorText() {
        return value == null ? null : value.getValue();
    }
}
