package com.dci.intellij.dbn.database.common;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseInterface;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.common.statement.CallableStatementOutput;
import com.dci.intellij.dbn.database.common.statement.StatementExecutionProcessor;
import com.intellij.openapi.diagnostic.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DatabaseInterfaceImpl implements DatabaseInterface{
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private String fileName;
    private DatabaseInterfaceProvider provider;
    protected Map<String, StatementExecutionProcessor> processors = new HashMap<String, StatementExecutionProcessor>();

    public DatabaseInterfaceImpl(String fileName, DatabaseInterfaceProvider provider) {
        this.fileName = fileName;
        this.provider = provider;
        reset();
    }

    @Override
    public void reset() {
        processors.clear();
        Document document = CommonUtil.loadXmlFile(getClass(), fileName);
        Element root = document.getRootElement();
        for (Object child : root.getChildren()) {
            Element element = (Element) child;
            StatementExecutionProcessor executionProcessor = new StatementExecutionProcessor(element, provider);
            String id = executionProcessor.getId();
            processors.put(id, executionProcessor);
        }
    }

    protected ResultSet executeQuery(@NotNull DBNConnection connection, String loaderId, @Nullable Object... arguments) throws SQLException {
        return executeQuery(connection, true, loaderId, arguments);
    }

    protected ResultSet executeQuery(@NotNull DBNConnection connection, boolean forceExecution, String loaderId, @Nullable Object... arguments) throws SQLException {
        StatementExecutionProcessor executionProcessor = getExecutionProcessor(loaderId);
        ResultSet result = executionProcessor.executeQuery(connection, forceExecution, arguments);
        checkDisposed(connection);
        return result;
    }

    protected <T extends CallableStatementOutput> T executeCall(@NotNull DBNConnection connection, @Nullable T outputReader, String loaderId, @Nullable Object... arguments) throws SQLException {
        StatementExecutionProcessor executionProcessor = getExecutionProcessor(loaderId);
        T result = executionProcessor.executeCall(connection, outputReader, arguments);
        checkDisposed(connection);
        return result;
    }

    protected boolean executeStatement(@NotNull DBNConnection connection, String loaderId, @Nullable Object... arguments) throws SQLException {
        StatementExecutionProcessor executionProcessor = getExecutionProcessor(loaderId);
        boolean result = executionProcessor.executeStatement(connection, arguments);
        checkDisposed(connection);
        return result;
    }

    protected void executeUpdate(@NotNull DBNConnection connection, String loaderId, @Nullable Object... arguments) throws SQLException {
        StatementExecutionProcessor executionProcessor = getExecutionProcessor(loaderId);
        executionProcessor.executeUpdate(connection, arguments);
        checkDisposed(connection);
    }

    @NotNull
    private StatementExecutionProcessor getExecutionProcessor(String loaderId) throws SQLException {
        StatementExecutionProcessor executionProcessor = processors.get(loaderId);
        if (executionProcessor == null) {
            DatabaseType databaseType = provider.getDatabaseType();
            String databaseTypeName = databaseType == DatabaseType.UNKNOWN ? "this" : databaseType.getName();
            throw new SQLException("Feature [" + loaderId + "] not implemented / supported for " + databaseTypeName + " database type");
        }
        return executionProcessor;
    }

    private void checkDisposed(DBNConnection connection) throws SQLException {
        Failsafe.nd(connection.getProject());
    }

    public DatabaseInterfaceProvider getProvider() {
        return provider;
    }
}
