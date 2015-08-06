package com.dci.intellij.dbn.database.common.statement;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.intellij.openapi.diagnostic.Logger;

public class StatementExecutionProcessor {
    private DatabaseInterfaceProvider interfaceProvider;
    private static final Logger LOGGER = LoggerFactory.createLogger();
    private String id;
    private boolean isQuery;
    private boolean isPreparedStatement;
    private int timeout;
    private List<StatementDefinition> statementDefinitions = new ArrayList<StatementDefinition>();
    private StatementDefinition statementDefinition;
    private SQLException lastException;
    public static final int DEFAULT_TIMEOUT = 30;

    public StatementExecutionProcessor(Element element, DatabaseInterfaceProvider interfaceProvider) {
        this.interfaceProvider = interfaceProvider;
        id = element.getAttributeValue("id");
        isQuery = Boolean.parseBoolean(element.getAttributeValue("is-query"));
        isPreparedStatement = Boolean.parseBoolean(element.getAttributeValue("is-prepared-statement"));
        String timeoutS = element.getAttributeValue("timeout");
        timeout = StringUtil.isEmpty(timeoutS) ? DEFAULT_TIMEOUT : Integer.parseInt(timeoutS);
        if (element.getChildren().isEmpty()) {
            String statementText = element.getContent(0).getValue().trim();
            readStatements(statementText, null);
        } else {
            for (Object object : element.getChildren()) {
                Element child = (Element) object;
                String statementText = child.getContent(0).getValue().trim();
                String prefixes = child.getAttributeValue("prefixes");
                readStatements(statementText, prefixes);
            }
        }
        if (statementDefinitions.size() == 1) {
            statementDefinition = statementDefinitions.get(0);
            statementDefinitions.clear();
            statementDefinitions = null;
        }
    }

    private void readStatements(String statementText, String prefixes) {
        if (prefixes == null) {
            StatementDefinition statementDefinition = new StatementDefinition(statementText, null, isPreparedStatement, false);
            statementDefinitions.add(statementDefinition);
        } else {
            StringTokenizer tokenizer = new StringTokenizer(prefixes, ",");
            while (tokenizer.hasMoreTokens()) {
                String prefix = tokenizer.nextToken().trim();
                boolean hasFallback = tokenizer.hasMoreTokens();
                StatementDefinition statementDefinition = new StatementDefinition(statementText, prefix, isPreparedStatement, hasFallback);
                statementDefinitions.add(statementDefinition);
            }
        }
    }

    public DatabaseInterfaceProvider getInterfaceProvider() {
        return interfaceProvider;
    }

    public String getId() {
        return id;
    }

    public ResultSet executeQuery(Connection connection, Object... arguments) throws SQLException {
        return executeQuery(connection, false, arguments);
    }

    public ResultSet executeQuery(Connection connection, boolean forceExecution, Object... arguments) throws SQLException {
        if (statementDefinition != null) {
            return executeQuery(statementDefinition, connection, forceExecution, arguments);
        } else {
            SQLException exception = null;
            for (StatementDefinition statementDefinition : statementDefinitions) {
                try {
                    return executeQuery(statementDefinition, connection, forceExecution, arguments);
                } catch (SQLException e){
                    exception = e;
                }
            }
            throw exception;
        }
    }

    private ResultSet executeQuery(final StatementDefinition statementDefinition, final Connection connection, boolean forceExecution, final Object... arguments) throws SQLException {
        if (forceExecution || statementDefinition.canExecute(connection)) {
            return new StatementExecutor<ResultSet>(timeout) {
                private Statement statement;
                @Override
                public ResultSet execute() throws Exception {
                    String statementText = null;
                    boolean executionSuccessful = true;
                    try {
                        if (SettingsUtil.isDebugEnabled) {
                            statementText = statementDefinition.prepareStatementText(arguments);
                            LOGGER.info("[DBN-INFO] Executing statement: " + statementText);
                        }
                        if (isPreparedStatement) {
                            PreparedStatement preparedStatement = statementDefinition.prepareStatement(connection, arguments);
                            preparedStatement.setQueryTimeout(timeout);
                            statement = preparedStatement;
                            return preparedStatement.executeQuery();
                        } else {
                            if (statementText == null)
                                statementText = statementDefinition.prepareStatementText(arguments);
                            statement = connection.createStatement();
                            statement.setQueryTimeout(timeout);
                            statement.execute(statementText);
                            if (isQuery) {
                                return statement.getResultSet();
                            } else {
                                ConnectionUtil.closeStatement(statement);
                                return null;
                            }
                        }

                    } catch (SQLException exception) {
                        executionSuccessful = false;

                        if (SettingsUtil.isDebugEnabled) LOGGER.info("[DBN-ERROR] Error executing statement: " + statementText + "\nCause: " + exception.getMessage());
                        if (interfaceProvider.getMessageParserInterface().isModelException(exception)) {
                            statementDefinition.setDisabled(true);
                            lastException = new SQLException("Model exception received while executing query '" + id +"'. " + exception.getMessage());
                        } else {
                            lastException = new SQLException("Too many failed attempts of executing query '" + id +"'. " + exception.getMessage());
                        }
                        ConnectionUtil.closeStatement(statement);
                        throw exception;
                    } finally {
                        statementDefinition.updateExecutionStatus(executionSuccessful);
                    }
                }

                @Override
                protected void handleTimeout() {
                    ConnectionUtil.closeStatement(statement);
                }
            }.start();
        } else {
            if (lastException == null) {
                throw new SQLException("Too many failed attempts of executing query '" + id + "'.");
            }
            throw lastException;
        }
    }

    public <T extends CallableStatementOutput> T executeCall(Connection connection, @Nullable T outputReader, Object... arguments) throws SQLException {
        if (statementDefinition != null) {
            return executeCall(statementDefinition, connection, outputReader, arguments);
        } else {
            SQLException exception = null;
            for (StatementDefinition statementDefinition : statementDefinitions) {
                try {
                    return executeCall(statementDefinition, connection, outputReader, arguments);
                } catch (SQLException e){
                    exception = e;
                }

            }
            throw exception;
        }
    }

    private <T extends CallableStatementOutput> T executeCall(final StatementDefinition statementDefinition, final Connection connection, @Nullable final T outputReader, final Object... arguments) throws SQLException {
        return new StatementExecutor<T>(timeout) {
            CallableStatement statement;
            @Override
            public T execute() throws Exception {
                String statementText = statementDefinition.prepareStatementText(arguments);
                if (SettingsUtil.isDebugEnabled) LOGGER.info("[DBN-INFO] Executing statement: " + statementText);

                statement = connection.prepareCall(statementText);
                try {
                    if (outputReader != null) outputReader.registerParameters(statement);
                    statement.setQueryTimeout(timeout);
                    statement.execute();
                    if (outputReader != null) outputReader.read(statement);
                    return outputReader;
                } catch (SQLException exception) {
                    if (SettingsUtil.isDebugEnabled)
                        LOGGER.info(
                                "[DBN-ERROR] Error executing statement: " + statementText +
                                        "\nCause: " + exception.getMessage());

                    throw exception;
                } finally {
                    ConnectionUtil.closeStatement(statement);
                }
            }

            @Override
            protected void handleTimeout() {
                ConnectionUtil.closeStatement(statement);
            }
        }.start();
    }

    public void executeUpdate(Connection connection, Object... arguments) throws SQLException {
        if (statementDefinition != null) {
            executeUpdate(statementDefinition, connection, arguments);
        } else {
            SQLException exception = null;
            for (StatementDefinition statementDefinition : statementDefinitions) {
                try {
                    executeUpdate(statementDefinition, connection, arguments);
                    return;
                } catch (SQLException e){
                    exception = e;
                }
            }
            throw exception;
        }
    }

    private void executeUpdate(final StatementDefinition statementDefinition, final Connection connection, final Object... arguments) throws SQLException {
        new StatementExecutor(timeout) {
            private Statement statement;
            @Override
            public Object execute() throws Exception {
                String statementText = statementDefinition.prepareStatementText(arguments);
                if (SettingsUtil.isDebugEnabled) LOGGER.info("[DBN-INFO] Executing statement: " + statementText);

                statement = connection.createStatement();
                try {
                    statement.setQueryTimeout(timeout);
                    statement.executeUpdate(statementText);
                } catch (SQLException exception) {
                    if (SettingsUtil.isDebugEnabled)
                        LOGGER.info(
                                "[DBN-ERROR] Error executing statement: " + statementText +
                                        "\nCause: " + exception.getMessage());

                    throw exception;
                } finally {
                    ConnectionUtil.closeStatement(statement);
                }
                return null;
            }

            @Override
            protected void handleTimeout() {
                ConnectionUtil.closeStatement(statement);
            }
        }.start();
    }

    public boolean executeStatement(Connection connection, Object... arguments) throws SQLException {
        if (statementDefinition != null) {
            return executeStatement(statementDefinition, connection, arguments);
        } else {
            SQLException exception = null;
            for (StatementDefinition statementDefinition : statementDefinitions) {
                try {
                    return executeStatement(statementDefinition, connection, arguments);
                } catch (SQLException e){
                    exception = e;
                }
            }
            throw exception;
        }
    }

    private boolean executeStatement(final StatementDefinition statementDefinition, final Connection connection, final Object... arguments) throws SQLException {
        return new StatementExecutor<Boolean>(timeout) {
            private Statement statement;
            @Override
            public Boolean execute() throws Exception {
                String statementText = statementDefinition.prepareStatementText(arguments);
                if (SettingsUtil.isDebugEnabled) LOGGER.info("[DBN-INFO] Executing statement: " + statementText);

                statement = connection.createStatement();
                try {
                    statement.setQueryTimeout(timeout);
                    return statement.execute(statementText);
                } catch (SQLException exception) {
                    if (SettingsUtil.isDebugEnabled)
                        LOGGER.info(
                                "[DBN-ERROR] Error executing statement: " + statementText +
                                        "\nCause: " + exception.getMessage());

                    throw exception;
                } finally {
                    ConnectionUtil.closeStatement(statement);
                }
            }

            @Override
            protected void handleTimeout() {
                ConnectionUtil.closeStatement(statement);
            }
        }.start();
    }
}
