package com.dci.intellij.dbn.database.common.execution;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.locale.Formatter;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.DBFunction;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.lookup.DBMethodRef;
import com.intellij.openapi.project.Project;

public abstract class MethodExecutionProcessorImpl<T extends DBMethod> implements MethodExecutionProcessor<T> {
    private DBMethodRef<T> method;

    protected MethodExecutionProcessorImpl(T method) {
        this.method = new DBMethodRef<T>(method);
    }

    @Nullable
    public T getMethod() {
        return (T) method.get();
    }

    public List<DBArgument> getArguments() {
        T method = getMethod();
        return method == null ? new ArrayList<DBArgument>() : method.getArguments();
    }

    public DBArgument getReturnArgument() {
        DBMethod method = getMethod();
        if (method instanceof DBFunction) {
            DBFunction function = (DBFunction) method;
            return function.getReturnArgument();
        }
        return null;
    }

    protected int getParametersCount() {
        return getArguments().size();
    }

    public void execute(MethodExecutionInput executionInput) throws SQLException {
        boolean usePoolConnection = executionInput.isUsePoolConnection();
        T method = getMethod();
        if (method != null) {
            ConnectionHandler connectionHandler = method.getConnectionHandler();
            DBSchema executionSchema = executionInput.getExecutionSchema();
            Connection connection = usePoolConnection ?
                    connectionHandler.getPoolConnection(executionSchema) :
                    connectionHandler.getStandaloneConnection(executionSchema);
            if (usePoolConnection) {
                connection.setAutoCommit(false);
            }
            execute(executionInput, connection);
        }
    }

    public void execute(MethodExecutionInput executionInput, Connection connection) throws SQLException {
        ConnectionHandler connectionHandler = null;
        boolean usePoolConnection = false;
        try {
            long startTime = System.currentTimeMillis();

            String command = buildExecutionCommand(executionInput);
            T method = getMethod();
            if (method != null) {
                connectionHandler = method.getConnectionHandler();
                usePoolConnection = executionInput.isUsePoolConnection();

                if (usePreparedStatement(method)) {
                    PreparedStatement preparedStatement = connection.prepareStatement(command);
                    bindParameters(executionInput, preparedStatement);

                    preparedStatement.setQueryTimeout(10);
                    preparedStatement.execute();
                    MethodExecutionResult executionResult = executionInput.getExecutionResult();
                    if (executionResult != null) {
                        loadValues(executionResult, preparedStatement);
                        executionResult.setExecutionDuration((int) (System.currentTimeMillis() - startTime));
                    }

                } else {
                    CallableStatement callableStatement = connection.prepareCall (command);

                    bindParameters(executionInput, callableStatement);
                    callableStatement.setQueryTimeout(10);
                    callableStatement.execute();

                    MethodExecutionResult executionResult = executionInput.getExecutionResult();
                    if (executionResult != null) {
                        loadValues(executionResult, callableStatement);
                        executionResult.setExecutionDuration((int) (System.currentTimeMillis() - startTime));
                    }
                }

                if (!usePoolConnection) connectionHandler.notifyChanges(method.getVirtualFile());
            }

        } finally {
            if (executionInput.isCommitAfterExecution()) {
                if (usePoolConnection) {
                    connection.commit();
                } else {
                    if (connectionHandler != null) connectionHandler.commit();
                }
            }
            if (connectionHandler != null && usePoolConnection) connectionHandler.freePoolConnection(connection);
        }
    }

    protected boolean usePreparedStatement(T method) {
        return false;
    }

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

    public void loadValues(MethodExecutionResult executionResult, CallableStatement callableStatement) throws SQLException {
        for (DBArgument argument : getArguments()) {
            if (argument.isOutput()) {
                Object result = callableStatement.getObject(argument.getPosition());
                executionResult.addArgumentValue(argument, result);
            }
        }
    }

    protected void bindParameters(MethodExecutionInput executionInput, PreparedStatement preparedStatement) throws SQLException {

    }

    public void loadValues(MethodExecutionResult executionResult, PreparedStatement preparedStatement) throws SQLException {

    }

    private Project getProject() {
        return getMethod().getProject();
    }

    protected void setParameterValue(PreparedStatement preparedStatement, int parameterIndex, DBDataType dataType, String stringValue) throws SQLException {
        try {
            Object value = null;
            if (StringUtil.isNotEmptyOrSpaces(stringValue))  {
                Formatter formatter = Formatter.getInstance(getProject());
                value = formatter.parseObject(dataType.getTypeClass(), stringValue);
                value = dataType.getNativeDataType().getDataTypeDefinition().convert(value);
            }
            dataType.setValueToPreparedStatement(preparedStatement, parameterIndex, value);

        } catch (SQLException e) {
            throw e;
        }  catch (Exception e) {
            throw new SQLException("Invalid value for data type " + dataType.getName() + " provided: \"" + stringValue + "\"");
        }
    }

    public abstract String buildExecutionCommand(MethodExecutionInput executionInput) throws SQLException;
}
