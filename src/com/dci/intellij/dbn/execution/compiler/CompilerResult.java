package com.dci.intellij.dbn.execution.compiler;

import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.common.notification.NotificationGroup;
import com.dci.intellij.dbn.common.notification.NotificationSupport;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ResourceUtil;
import com.dci.intellij.dbn.database.DatabaseInterface;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompilerResult implements Disposable, NotificationSupport {

    private final DBObjectRef<DBSchemaObject> objectRef;
    private final List<CompilerMessage> compilerMessages = new ArrayList<>();
    private CompilerAction compilerAction;
    private boolean error = false;

    public CompilerResult(CompilerAction compilerAction, ConnectionHandler connectionHandler, DBSchema schema, DBObjectType objectType, String objectName) {
        objectRef = new DBObjectRef<>(schema.getRef(), objectType, objectName);
        init(connectionHandler, schema, objectName, compilerAction);
    }

    public CompilerResult(CompilerAction compilerAction, DBSchemaObject object) {
        objectRef = DBObjectRef.of(object);
        init(object.getConnectionHandler(), object.getSchema(), object.getName(), compilerAction);
    }

    public CompilerResult(CompilerAction compilerAction, DBSchemaObject object, DBContentType contentType, String errorMessage) {
        this.compilerAction = compilerAction;
        objectRef = DBObjectRef.of(object);
        CompilerMessage compilerMessage = new CompilerMessage(this, contentType, errorMessage, MessageType.ERROR);
        compilerMessages.add(compilerMessage);
    }

    private void init(ConnectionHandler connectionHandler, DBSchema schema, String objectName, CompilerAction compilerAction) {
        this.compilerAction = compilerAction;
        DBContentType contentType = compilerAction.getContentType();

        try {
            DatabaseInterface.run(true,
                    connectionHandler,
                    (provider, connection) -> {
                        ResultSet resultSet = null;
                        try {
                            resultSet = provider.getMetadataInterface().loadCompileObjectErrors(
                                    schema.getName(),
                                    objectName,
                                    connection);

                            while (resultSet != null && resultSet.next()) {
                                CompilerMessage errorMessage = new CompilerMessage(this, resultSet);
                                error = true;
                                if (/*!compilerAction.isDDL() || */contentType.isBundle() || contentType == errorMessage.getContentType()) {
                                    compilerMessages.add(errorMessage);
                                }
                            }
                        } finally{
                            ResourceUtil.close(resultSet);
                        }
                    });
        } catch (SQLException e) {
            sendErrorNotification(
                    NotificationGroup.COMPILER,
                    "Failed to read compiler result: {0}", e);
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
        return error;
    }

    public boolean isSingleMessage() {
        return compilerMessages.size() == 1;
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
