package com.dci.intellij.dbn.execution.compiler;

import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CompilerResult implements Disposable {
    private DBSchemaObject object;
    private List<CompilerMessage> compilerMessages = new ArrayList<CompilerMessage>();
    private boolean isError = false;

    public CompilerResult(DBSchemaObject object) {
        this.object = object;
        Connection connection = null;
        ResultSet resultSet = null;
        List<CompilerMessage> echoMessages = new ArrayList<CompilerMessage>();
        ConnectionHandler connectionHandler = object.getConnectionHandler();
        try {
            connection = connectionHandler.getPoolConnection();
            resultSet = connectionHandler.getInterfaceProvider().getMetadataInterface().loadCompileObjectErrors(
                    object.getSchema().getName(),
                    object.getName(),
                    connection);

            while (resultSet != null && resultSet.next()) {
                CompilerMessage errorMessage = new CompilerMessage(this, resultSet);
                isError = true;
                if (errorMessage.isEcho()) {
                    echoMessages.add(errorMessage);
                } else {
                    compilerMessages.add(errorMessage);
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally{
            connectionHandler.freePoolConnection(connection);
            ConnectionUtil.closeResultSet(resultSet);
        }

        if (compilerMessages.size() == 0) {
            if (echoMessages.size() > 0) {
                compilerMessages.addAll(echoMessages);
                isError = true;
            } else {
                compilerMessages.add(new CompilerMessage(this, "The " + getObject().getQualifiedNameWithType() + " was updated successfully."));
            }
        }
    }

    public boolean isError() {
        return isError;
    }

    public CompilerResult(DBSchemaObject object, String errorMessage) {
        this.object = object;
        CompilerMessage message = new CompilerMessage(this, errorMessage, MessageType.ERROR);
        compilerMessages.add(message);
    }

    public List<CompilerMessage> getCompilerMessages() {
        return compilerMessages;
    }

    public DBSchemaObject getObject() {
        return object;
    }

    public Project getProject() {
        return object.getProject();
    }

    public void dispose() {
        compilerMessages.clear();
        object = null;
    }
}
