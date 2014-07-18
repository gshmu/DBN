package com.dci.intellij.dbn.database.postgres.execution;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.database.common.execution.MethodExecutionProcessorImpl;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.DBFunction;
import com.dci.intellij.dbn.object.DBMethod;

public class PostgresMethodExecutionProcessor extends MethodExecutionProcessorImpl<DBMethod> {
    public PostgresMethodExecutionProcessor(DBMethod method) {
        super(method);
    }

    @Override
    public String buildExecutionCommand(MethodExecutionInput executionInput) throws SQLException {
        StringBuilder buffer = new StringBuilder();
        if (usePreparedStatement(executionInput.getMethod())) {
            buffer.append("select * from ");
            buffer.append(getMethod().getQualifiedName());
            buffer.append("(");
            for (int i=1; i<getParametersCount(); i++) {
                if (i>1) buffer.append(",");
                buffer.append("?");
            }
            buffer.append(")");

        } else {
            buffer.append("{ ? = call ");
            buffer.append(getMethod().getQualifiedName());
            buffer.append("(");
            for (int i=1; i<getParametersCount(); i++) {
                if (i>1) buffer.append(",");
                buffer.append("?");
            }
            buffer.append(")}");
        }

        return buffer.toString();
    }

    @Override
    protected void bindParameters(MethodExecutionInput executionInput, CallableStatement callableStatement) throws SQLException {
        for (DBArgument argument : getArguments()) {
            DBDataType dataType = argument.getDataType();
            if (argument.isInput()) {
                String stringValue = executionInput.getInputValue(argument);
                setParameterValue(callableStatement, argument.getPosition(), dataType, stringValue);
            }
            if (argument.isOutput()) {
                callableStatement.registerOutParameter(argument.getPosition(), dataType.getSqlType());
            }
        }
    }

    @Override
    protected void bindParameters(MethodExecutionInput executionInput, PreparedStatement preparedStatement) throws SQLException {
        List<DBArgument> arguments = getArguments();
        for (int i = 1; i < arguments.size(); i++) {
            DBArgument argument = arguments.get(i);
            DBDataType dataType = argument.getDataType();
            if (argument.isInput()) {
                String stringValue = executionInput.getInputValue(argument);
                setParameterValue(preparedStatement, argument.getPosition() -1, dataType, stringValue);
            }
        }
    }

    @Override
    public void loadValues(MethodExecutionResult executionResult, CallableStatement callableStatement) throws SQLException {
        for (DBArgument argument : getArguments()) {
            if (argument.isOutput()) {
                Object result = callableStatement.getObject(argument.getPosition());
                executionResult.addArgumentValue(argument, result);
            }
        }
    }

    @Override
    public void loadValues(MethodExecutionResult executionResult, PreparedStatement preparedStatement) throws SQLException {
        DBArgument returnArgument = getReturnArgument(executionResult.getMethod());
        executionResult.addArgumentValue(returnArgument, preparedStatement.getResultSet());
    }

    @Override
    protected boolean usePreparedStatement(DBMethod method) {
        DBArgument returnArgument = getReturnArgument(method);
        return returnArgument != null && returnArgument.getDataType().isSet();
    }

    public DBArgument getReturnArgument(DBMethod method) {
        if (method instanceof DBFunction) {
            DBFunction function = (DBFunction) method;
            return function.getReturnArgument();
        }
        return null;

    }
}