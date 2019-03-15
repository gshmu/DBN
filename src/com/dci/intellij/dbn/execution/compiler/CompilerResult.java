package com.dci.intellij.dbn.execution.compiler;

import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompilerResult implements Disposable {
    private DBObjectRef<DBSchemaObject> objectRef;
    private List<CompilerMessage> compilerMessages = new ArrayList<>();
    private boolean isError = false;
    private CompilerAction compilerAction;

    public CompilerResult(CompilerAction compilerAction, ConnectionHandler connectionHandler, DBSchema schema, DBObjectType objectType, String objectName) {
        objectRef = new DBObjectRef<>(schema.getRef(), objectType, objectName);
        init(connectionHandler, schema, objectName, compilerAction);
    }

    public CompilerResult(CompilerAction compilerAction, DBSchemaObject object) {
        objectRef = DBObjectRef.from(object);
        init(object.getConnectionHandler(), object.getSchema(), object.getName(), compilerAction);
    }

    public CompilerResult(CompilerAction compilerAction, DBSchemaObject object, DBContentType contentType, String errorMessage) {
        this.compilerAction = compilerAction;
        objectRef = DBObjectRef.from(object);
        CompilerMessage compilerMessage = new CompilerMessage(this, contentType, errorMessage, MessageType.ERROR);
        compilerMessages.add(compilerMessage);
    }

    private void init(ConnectionHandler connectionHandler, DBSchema schema, String objectName, CompilerAction compilerAction) {
        this.compilerAction = compilerAction;
        DBNConnection connection = null;
        ResultSet resultSet = null;
        DBContentType contentType = compilerAction.getContentType();
        try {
            connection = connectionHandler.getPoolConnection(true);
            resultSet = connectionHandler.getInterfaceProvider().getMetadataInterface().loadCompileObjectErrors(
                    schema.getName(),
                    objectName,
                    connection);

            while (resultSet != null && resultSet.next()) {
                CompilerMessage errorMessage = new CompilerMessage(this, resultSet);
                isError = true;
                if (/*!compilerAction.isDDL() || */contentType.isBundle() || contentType == errorMessage.getContentType()) {
                    compilerMessages.add(errorMessage);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally{
            ConnectionUtil.close(resultSet);
            connectionHandler.freePoolConnection(connection);
        }

        if (compilerMessages.size() == 0) {
            String contentDesc =
                    contentType == DBContentType.CODE_SPEC ? "spec of " :
                    contentType == DBContentType.CODE_BODY ? "body of " : "";

            String message = "The " + contentDesc + objectRef.getQualifiedNameWithType() + " was " + (compilerAction.isSave() ? "updated" : "compiled") + " successfully.";
            CompilerMessage compilerMessage = new CompilerMessage(this, contentType, message);
            compilerMessages.add(compilerMessage);
        } else {
            Collections.sort(compilerMessages);
        }
    }

    public CompilerAction getCompilerAction() {
        return compilerAction;
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
        return objectRef.objectType;
    }

    public DBObjectRef<DBSchemaObject> getObjectRef() {
        return objectRef;
    }

    @Override
    public void dispose() {
        compilerMessages.clear();
    }

    public Project getProject() {
        DBSchemaObject object = DBObjectRef.get(objectRef);
        if (object == null) {
            ConnectionHandler connectionHandler = objectRef.resolveConnectionHandler();
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
