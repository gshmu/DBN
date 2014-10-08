package com.dci.intellij.dbn.execution.compiler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;

public class CompilerResult implements Disposable {
    private DBObjectRef<DBSchemaObject> objectRef;
    private List<CompilerMessage> compilerMessages = new ArrayList<CompilerMessage>();
    private boolean isError = false;
    private CompilerAction sourceAction;

    public CompilerResult(ConnectionHandler connectionHandler, DBSchema schema, DBObjectType objectType, String objectName, CompilerAction sourceAction) {
        objectRef = new DBObjectRef<DBSchemaObject>(schema.getRef(), objectType, objectName);
        init(connectionHandler, schema, objectName, sourceAction);
    }

    public CompilerResult(DBSchemaObject object, CompilerAction sourceAction) {
        objectRef = DBObjectRef.from(object);
        init(object.getConnectionHandler(), object.getSchema(), object.getName(), sourceAction);
    }

    private void init(ConnectionHandler connectionHandler, DBSchema schema, String objectName, CompilerAction sourceAction) {
        this.sourceAction = sourceAction;
        Connection connection = null;
        ResultSet resultSet = null;
        try {
            connection = connectionHandler.getPoolConnection();
            resultSet = connectionHandler.getInterfaceProvider().getMetadataInterface().loadCompileObjectErrors(
                    schema.getName(),
                    objectName,
                    connection);

            while (resultSet != null && resultSet.next()) {
                CompilerMessage errorMessage = new CompilerMessage(this, resultSet);
                isError = true;
                if (!sourceAction.isDDL() || sourceAction.getContentType() == errorMessage.getContentType()) {
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
            CompilerMessage compilerMessage = new CompilerMessage(this, "The " + objectRef.getQualifiedNameWithType() + " was " + (sourceAction.getType() == CompilerAction.Type.SAVE ? "updated" : "compiled") +  " successfully.");
            compilerMessages.add(compilerMessage);
        }
    }

    public CompilerAction getSourceAction() {
        return sourceAction;
    }

    public CompilerResult(DBSchemaObject object, String errorMessage) {
        objectRef = DBObjectRef.from(object);
        CompilerMessage compilerMessage = new CompilerMessage(this, errorMessage, MessageType.ERROR);
        compilerMessages.add(compilerMessage);
    }

    public boolean isError() {
        return isError;
    }

    public List<CompilerMessage> getCompilerMessages() {
        return compilerMessages;
    }

    @Nullable
    public DBSchemaObject getObject() {
        return DBObjectRef.get(objectRef);
    }

    DBObjectType getObjectType() {
        return objectRef.getObjectType();
    }

    public DBObjectRef<DBSchemaObject> getObjectRef() {
        return objectRef;
    }

    public void dispose() {
        compilerMessages.clear();
    }

    public Project getProject() {
        DBSchemaObject object = DBObjectRef.get(objectRef);
        if (object == null) {
            ConnectionHandler connectionHandler = objectRef.lookupConnectionHandler();
            if (connectionHandler != null) return connectionHandler.getProject();
        } else {
            return object.getProject();
        }
        return null;
    }

    public boolean hasErrors() {
        for (CompilerMessage compilerMessage : compilerMessages) {
            if (compilerMessage.getType() == MessageType.ERROR) {
                return true;
            }
        }
        return false;
    }
}
