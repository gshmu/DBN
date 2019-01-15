package com.dci.intellij.dbn.database.common.execution;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.locale.Formatter;
import com.dci.intellij.dbn.common.thread.CancellableDatabaseCall;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.connection.SessionId;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.DBNPreparedStatement;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.execution.ExecutionContext;
import com.dci.intellij.dbn.execution.ExecutionOption;
import com.dci.intellij.dbn.execution.ExecutionOptions;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.logging.DatabaseLoggingManager;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.options.MethodExecutionSettings;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class MethodExecutionProcessorImpl<T extends DBMethod> implements MethodExecutionProcessor<T> {
    private static final Logger LOGGER = LoggerFactory.createLogger();
    private DBObjectRef<T> method;

    protected MethodExecutionProcessorImpl(T method) {
        this.method = new DBObjectRef<T>(method);
    }

    @NotNull
    public T getMethod() {
        return DBObjectRef.getnn(method);
    }

    public List<DBArgument> getArguments() {
        T method = getMethod();
        return method.getArguments();
    }

    protected int getArgumentsCount() {
        return getArguments().size();
    }

    protected DBArgument getReturnArgument() {
        DBMethod method = getMethod();
        return method.getReturnArgument();
    }


    public void execute(MethodExecutionInput executionInput, DBDebuggerType debuggerType) throws SQLException {
        executionInput.initExecution(debuggerType);
        ConnectionHandler connectionHandler = getConnectionHandler();
        SessionId targetSessionId = executionInput.getTargetSessionId();
        DBSchema targetSchema = executionInput.getTargetSchema();
        DBNConnection connection = connectionHandler.getConnection(targetSessionId, targetSchema);

        if (targetSessionId == SessionId.POOL) {
            ConnectionUtil.setAutoCommit(connection, false);
        }

        execute(executionInput, connection, debuggerType);
    }

    public void execute(final MethodExecutionInput executionInput, @NotNull DBNConnection connection, DBDebuggerType debuggerType) throws SQLException {
        executionInput.initExecution(debuggerType);
        ExecutionOptions options = executionInput.getOptions();
        final ConnectionHandler connectionHandler = getConnectionHandler();
        SessionId targetSessionId = executionInput.getTargetSessionId();

        boolean loggingEnabled = debuggerType != DBDebuggerType.JDBC && options.is(ExecutionOption.ENABLE_LOGGING);
        Project project = getProject();
        final DatabaseLoggingManager loggingManager = DatabaseLoggingManager.getInstance(project);

        ExecutionContext context = executionInput.getExecutionContext(true);
        try {
            String command = buildExecutionCommand(executionInput);
            T method = getMethod();
            loggingEnabled = loggingEnabled && loggingManager.supportsLogging(connectionHandler);
            if (loggingEnabled) {
                loggingEnabled = loggingManager.enableLogger(connectionHandler, connection);
            }

            final DBNPreparedStatement statement = isQuery() ?
                    connection.prepareStatement(command) :
                    connection.prepareCall(command);

            context.setConnection(connection);
            context.setStatement(statement);

            bindParameters(executionInput, statement);

            MethodExecutionSettings methodExecutionSettings = ExecutionEngineSettings.getInstance(project).getMethodExecutionSettings();
            int timeout = debuggerType.isDebug() ?
                    methodExecutionSettings.getDebugExecutionTimeout() :
                    methodExecutionSettings.getExecutionTimeout();

            statement.setQueryTimeout(timeout);
            MethodExecutionResult executionResult = new CancellableDatabaseCall<MethodExecutionResult>(connectionHandler, connection, timeout, TimeUnit.SECONDS) {
                @Override
                public MethodExecutionResult execute() throws Exception {
                    statement.execute();
                    return executionInput.getExecutionResult();
                }

                @Override
                public void cancel() throws Exception {
                    ConnectionUtil.cancel(statement);
                }
            }.start();

            if (executionResult != null) {
                loadValues(executionResult, statement);
                executionResult.calculateExecDuration();

                if (loggingEnabled) {
                    String logOutput = loggingManager.readLoggerOutput(connectionHandler, connection);
                    executionResult.setLogOutput(logOutput);
                }
            }

            if (targetSessionId != SessionId.POOL) connection.notifyDataChanges(method.getVirtualFile());
        } catch (SQLException e) {
            ConnectionUtil.cancel(context.getStatement());
            throw e;
        } finally {
            if (loggingEnabled) {
                loggingManager.disableLogger(connectionHandler, connection);
            }

            if (options.is(ExecutionOption.COMMIT_AFTER_EXECUTION)) {
                ConnectionUtil.commit(connection);
            } else {
                if (targetSessionId == SessionId.POOL) {
                    ConnectionUtil.rollback(connection);
                }
            }

            if (connection.isDebugConnection()) {
                ConnectionUtil.close(connection);

            } else if (connection.isPoolConnection()) {
                connectionHandler.freePoolConnection(connection);
            }
        }
    }

    @NotNull
    private ConnectionHandler getConnectionHandler() {
        return FailsafeUtil.get(getMethod().getConnectionHandler());
    }

    protected boolean isQuery() {
        return false;
    }

    protected void bindParameters(MethodExecutionInput executionInput, PreparedStatement preparedStatement) throws SQLException {
        for (DBArgument argument : getArguments()) {
            DBDataType dataType = argument.getDataType();
            if (argument.isInput()) {
                String stringValue = executionInput.getInputValue(argument);
                setParameterValue(preparedStatement, argument.getPosition(), dataType, stringValue);
            }
            if (argument.isOutput() && preparedStatement instanceof CallableStatement) {
                CallableStatement callableStatement = (CallableStatement) preparedStatement;
                callableStatement.registerOutParameter(argument.getPosition(), dataType.getSqlType());
            }
        }
    }

    public void loadValues(MethodExecutionResult executionResult, DBNPreparedStatement preparedStatement) throws SQLException {
        for (DBArgument argument : getArguments()) {
            if (argument.isOutput() && preparedStatement instanceof CallableStatement) {
                CallableStatement callableStatement = (CallableStatement) preparedStatement;
                Object result = callableStatement.getObject(argument.getPosition());
                executionResult.addArgumentValue(argument, result);
            }
        }
    }

    private Project getProject() {
        T method = getMethod();
        return method.getProject();
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
