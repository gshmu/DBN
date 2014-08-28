package com.dci.intellij.dbn.database.common;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.jdom.Document;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.database.DatabaseInterface;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.common.statement.CallableStatementOutput;
import com.dci.intellij.dbn.database.common.statement.StatementExecutionProcessor;

public class DatabaseInterfaceImpl implements DatabaseInterface{
    private String fileName;
    private DatabaseInterfaceProvider provider;
    protected Map<String, StatementExecutionProcessor> processors = new HashMap<String, StatementExecutionProcessor>();

    public DatabaseInterfaceImpl(String fileName, DatabaseInterfaceProvider provider) {
        this.fileName = fileName;
        this.provider = provider;
        reset();
    }

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

    protected ResultSet executeQuery(Connection connection, String loaderId, @Nullable Object... arguments) throws SQLException {
        return executeQuery(connection, true, loaderId, arguments);
    }

    protected ResultSet executeQuery(Connection connection, boolean forceExecution, String loaderId, @Nullable Object... arguments) throws SQLException {
        StatementExecutionProcessor executionProcessor = processors.get(loaderId);
        return executionProcessor.executeQuery(connection, forceExecution, arguments);
    }

    protected <T extends CallableStatementOutput> T executeCall(Connection connection, @Nullable T outputReader, String loaderId, @Nullable Object... arguments) throws SQLException {
        StatementExecutionProcessor executionProcessor = processors.get(loaderId);
        return executionProcessor.executeCall(connection, outputReader, arguments);
    }

    protected boolean executeStatement(Connection connection, String loaderId, @Nullable Object... arguments) throws SQLException {
        StatementExecutionProcessor executionProcessor = processors.get(loaderId);
        return executionProcessor.executeStatement(connection, arguments);
    }

    protected void executeUpdate(Connection connection, String loaderId, @Nullable Object... arguments) throws SQLException {
        StatementExecutionProcessor executionProcessor = processors.get(loaderId);
        executionProcessor.executeUpdate(connection, arguments);
    }

    public DatabaseInterfaceProvider getProvider() {
        return provider;
    }
}
