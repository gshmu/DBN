package com.dci.intellij.dbn.database.postgres.execution;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.List;

import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.database.common.execution.MethodExecutionProcessorImpl;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.DBMethod;

public class PostgresMethodExecutionProcessor extends MethodExecutionProcessorImpl<DBMethod> {
    public PostgresMethodExecutionProcessor(DBMethod method) {
        super(method);
    }

    @Override
    public String buildExecutionCommand(MethodExecutionInput executionInput) throws SQLException {
        StringBuilder buffer = new StringBuilder();
        buffer.append("{ ? = call ");
        buffer.append(getMethod().getQualifiedName());
        buffer.append("(");
        for (int i=1; i<getParametersCount(); i++) {
            if (i>1) buffer.append(",");
            buffer.append("?");
        }
        buffer.append(")}");
        return buffer.toString();
    }

    @Override
    protected void prepareCall(MethodExecutionInput executionInput, CallableStatement callableStatement) throws SQLException {
        List<DBArgument> arguments = getArguments();
        for (DBArgument argument : arguments) {
            DBDataType dataType = argument.getDataType();
            if (argument.isInput()) {
                String stringValue = executionInput.getInputValue(argument);
                setParameterValue(callableStatement, argument.getPosition(), dataType, stringValue);
            }
            if (argument.isOutput()) {
                callableStatement.registerOutParameter(argument.getPosition(), dataType.getSqlType());
            }
        }
        //callableStatement.registerOutParameter(lastPosition + 1, arguments.get(0).getDataType().getSqlType());
    }

    @Override
    public void loadValues(MethodExecutionResult executionResult, CallableStatement callableStatement) throws SQLException {
        List<DBArgument> arguments = getArguments();
        for (DBArgument argument : arguments) {
            if (argument.isOutput()) {
                Object result = callableStatement.getObject(argument.getPosition());
                executionResult.addArgumentValue(argument, result);
            }
        }
    }
}