package com.dci.intellij.dbn.database.common.statement;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ResourceUtil;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseActivityTrace;
import com.dci.intellij.dbn.database.DatabaseCompatibility;
import com.dci.intellij.dbn.database.DatabaseInterface;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.intellij.openapi.diagnostic.Logger;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicReference;

import static com.dci.intellij.dbn.DatabaseNavigator.DEBUG;

public class StatementExecutionProcessor {
    private static final Logger LOGGER = LoggerFactory.createLogger();
    public static final SQLFeatureNotSupportedException NO_STATEMENT_DEFINITION_EXCEPTION = new SQLFeatureNotSupportedException("No statement definition found");

    private final DatabaseInterfaceProvider interfaceProvider;
    private final String id;
    private final boolean query;
    private final boolean prepared;
    private int timeout = 30;
    private List<StatementDefinition> statementDefinitions = new ArrayList<>();
    private StatementDefinition statementDefinition;

    public StatementExecutionProcessor(Element element, DatabaseInterfaceProvider interfaceProvider) {
        this.interfaceProvider = interfaceProvider;
        this.id = element.getAttributeValue("id");
        this.query = Boolean.parseBoolean(element.getAttributeValue("is-query"));
        this.prepared = Boolean.parseBoolean(element.getAttributeValue("is-prepared-statement"));
        String customTimeout = element.getAttributeValue("timeout");
        if (StringUtil.isNotEmpty(customTimeout)) {
            timeout = Integer.parseInt(customTimeout);
        }

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
            StatementDefinition statementDefinition = new StatementDefinition(statementText, null, prepared, false);
            statementDefinitions.add(statementDefinition);
        } else {
            StringTokenizer tokenizer = new StringTokenizer(prefixes, ",");
            while (tokenizer.hasMoreTokens()) {
                String prefix = tokenizer.nextToken().trim();
                boolean hasFallback = tokenizer.hasMoreTokens();
                StatementDefinition statementDefinition = new StatementDefinition(statementText, prefix, prepared, hasFallback);
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

    public ResultSet executeQuery(@NotNull DBNConnection connection, boolean forceExecution, Object... arguments) throws SQLException {
        if (statementDefinition != null) {
            return executeQuery(statementDefinition, connection, forceExecution, arguments);
        } else {
            SQLException exception = NO_STATEMENT_DEFINITION_EXCEPTION;
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

    private ResultSet executeQuery(
            @NotNull StatementDefinition statementDefinition,
            @NotNull DBNConnection connection,
            boolean forceExecution,
            Object... arguments) throws SQLException {

        boolean hasFallback = statementDefinition.hasFallback();
        DatabaseCompatibility compatibility = DatabaseInterface.getConnectionHandler().getCompatibility();
        DatabaseActivityTrace activityTrace = compatibility.getActivityTrace(statementDefinition.getId());

        if (forceExecution || activityTrace.canExecute(hasFallback)) {
            AtomicReference<Statement> statementRef = new AtomicReference<>();
            return StatementExecutor.execute(
                    timeout,
                    statementRef,
                    () -> {
                        Statement statement = null;
                        ResultSet resultSet = null;
                        String statementText = null;
                        try {
                            activityTrace.init();
                            if (DEBUG) {
                                statementText = statementDefinition.prepareStatementText(arguments);
                                LOGGER.info("[DBN-INFO] Executing statement: " + statementText);
                            }
                            if (prepared) {
                                PreparedStatement preparedStatement = statementDefinition.prepareStatement(connection, arguments);
                                statement = preparedStatement;
                                preparedStatement.setQueryTimeout(timeout);
                                resultSet = preparedStatement.executeQuery();
                                return resultSet;
                            } else {
                                if (statementText == null)
                                    statementText = statementDefinition.prepareStatementText(arguments);
                                statement = connection.createStatement();
                                statement.setQueryTimeout(timeout);
                                statement.execute(statementText);
                                if (query) {
                                    try {
                                        resultSet = statement.getResultSet();
                                        return resultSet;
                                    } catch (SQLException e) {
                                        ResourceUtil.close(statement);
                                        return null;
                                    }
                                } else {
                                    ResourceUtil.close(statement);
                                    return null;
                                }
                            }
                        } catch (SQLException exception) {
                            ResourceUtil.close(statement);
                            String message = exception.getMessage();
                            if (DEBUG) LOGGER.info("[DBN-ERROR] Error executing statement: " + statementText + "\nCause: " + message);

                            boolean isModelException = interfaceProvider.getMessageParserInterface().isModelException(exception);
                            SQLException traceException =
                                    isModelException ?
                                            new SQLException("Model exception received while executing query '" + id +"'. " + message) :
                                            new SQLException("Too many failed attempts of executing query '" + id +"'. " + message);

                            activityTrace.fail(traceException, isModelException);
                            throw exception;
                        } finally {
                            activityTrace.release();
                            if (resultSet == null) {
                                ResourceUtil.close(statement);
                            }
                        }
                    });
        } else {
            throw CommonUtil.nvl(
                    activityTrace.getException(),
                    () -> new SQLException("Too many failed attempts of executing query '" + id + "'."));
        }
    }

    public <T extends CallableStatementOutput> T executeCall(
            @NotNull DBNConnection connection,
            @Nullable T outputReader,
            Object... arguments) throws SQLException {

        if (statementDefinition != null) {
            return executeCall(statementDefinition, connection, outputReader, arguments);
        } else {
            SQLException exception = NO_STATEMENT_DEFINITION_EXCEPTION;
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

    private <T extends CallableStatementOutput> T executeCall(
            @NotNull StatementDefinition statementDefinition,
            @NotNull DBNConnection connection,
            @Nullable T outputReader,
            Object... arguments) throws SQLException {

        AtomicReference<Statement> statementRef = new AtomicReference<>();
        return StatementExecutor.execute(
                timeout,
                statementRef,
                () -> {
                    String statementText = statementDefinition.prepareStatementText(arguments);
                    if (DEBUG) LOGGER.info("[DBN-INFO] Executing statement: " + statementText);

                    CallableStatement statement = connection.prepareCall(statementText);
                    statementRef.set(statement);
                    try {
                        if (outputReader != null) outputReader.registerParameters(statement);
                        statement.setQueryTimeout(timeout);
                        statement.execute();
                        if (outputReader != null) outputReader.read(statement);
                        return outputReader;
                    } catch (SQLException exception) {
                        if (DEBUG)
                            LOGGER.info(
                                    "[DBN-ERROR] Error executing statement: " + statementText +
                                            "\nCause: " + exception.getMessage());

                        throw exception;
                    } finally {
                        ResourceUtil.close(statement);
                    }
                });
    }

    public void executeUpdate(DBNConnection connection, Object... arguments) throws SQLException {
        if (statementDefinition != null) {
            executeUpdate(statementDefinition, connection, arguments);
        } else {
            SQLException exception = NO_STATEMENT_DEFINITION_EXCEPTION;
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

    private void executeUpdate(
            @NotNull StatementDefinition statementDefinition,
            @NotNull DBNConnection connection,
            Object... arguments) throws SQLException {


        AtomicReference<Statement> statementRef = new AtomicReference<>();
        StatementExecutor.execute(
                timeout,
                statementRef,
                () -> {
                    String statementText = statementDefinition.prepareStatementText(arguments);
                    if (DEBUG) LOGGER.info("[DBN-INFO] Executing statement: " + statementText);

                    Statement statement = connection.createStatement();
                    statementRef.set(statement);
                    try {
                        statement.setQueryTimeout(timeout);
                        statement.executeUpdate(statementText);
                    } catch (SQLException exception) {
                        if (DEBUG)
                            LOGGER.info(
                                    "[DBN-ERROR] Error executing statement: " + statementText +
                                            "\nCause: " + exception.getMessage());

                        throw exception;
                    } finally {
                        ResourceUtil.close(statement);
                    }
                    return null;
                });
    }

    public boolean executeStatement(@NotNull DBNConnection connection, Object... arguments) throws SQLException {
        if (statementDefinition != null) {
            return executeStatement(statementDefinition, connection, arguments);
        } else {
            SQLException exception = NO_STATEMENT_DEFINITION_EXCEPTION;
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

    private boolean executeStatement(
            @NotNull StatementDefinition statementDefinition,
            @NotNull DBNConnection connection,
            Object... arguments) throws SQLException {

        AtomicReference<Statement> statementRef = new AtomicReference<>();
        return StatementExecutor.execute(
                timeout,
                statementRef,
                () -> {
                    String statementText = statementDefinition.prepareStatementText(arguments);
                    if (DEBUG) LOGGER.info("[DBN-INFO] Executing statement: " + statementText);

                    Statement statement = connection.createStatement();
                    statementRef.set(statement);
                    try {
                        statement.setQueryTimeout(timeout);
                        return statement.execute(statementText);
                    } catch (SQLException exception) {
                        if (DEBUG)
                            LOGGER.info(
                                    "[DBN-ERROR] Error executing statement: " + statementText +
                                            "\nCause: " + exception.getMessage());

                        throw exception;
                    } finally {
                        ResourceUtil.close(statement);
                    }
                });
    }
}
