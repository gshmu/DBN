package com.dci.intellij.dbn.execution.compiler;

import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.common.notification.NotificationGroup;
import com.dci.intellij.dbn.common.notification.NotificationSupport;
import com.dci.intellij.dbn.common.util.Naming;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.Resources;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterfaceInvoker;
import com.dci.intellij.dbn.database.interfaces.DatabaseMetadataInterface;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.dci.intellij.dbn.common.Priority.HIGH;

@Getter
public class CompilerResult implements Disposable, NotificationSupport {
    private final DBObjectRef<DBSchemaObject> object;
    private final List<CompilerMessage> compilerMessages = new ArrayList<>();
    private CompilerAction compilerAction;
    private boolean error = false;

    public CompilerResult(CompilerAction compilerAction, ConnectionHandler connection, DBSchema schema, DBObjectType objectType, String objectName) {
        object = new DBObjectRef<>(schema.ref(), objectType, objectName);
        init(connection, schema, objectName, objectType, compilerAction);
    }

    public CompilerResult(CompilerAction compilerAction, DBSchemaObject object) {
        this.object = DBObjectRef.of(object);
        init(object.getConnection(), object.getSchema(), object.getName(), object.getObjectType(), compilerAction);
    }

    public CompilerResult(CompilerAction compilerAction, DBSchemaObject object, DBContentType contentType, String errorMessage) {
        this.compilerAction = compilerAction;
        this.object = DBObjectRef.of(object);
        CompilerMessage compilerMessage = new CompilerMessage(this, contentType, errorMessage, MessageType.ERROR);
        compilerMessages.add(compilerMessage);
    }

    private void init(ConnectionHandler connection, DBSchema schema, String objectName, DBObjectType objectType, CompilerAction compilerAction) {
        this.compilerAction = compilerAction;
        DBContentType contentType = compilerAction.getContentType();
        String qualifiedObjectName = Naming.getQualifiedObjectName(objectType, objectName, schema);

        try {
            DatabaseInterfaceInvoker.execute(HIGH,
                    "Loading compiler data",
                    "Loading compile results for " + qualifiedObjectName,
                    connection.createInterfaceContext(), conn ->
                            loadCompilerErrors(connection, schema, objectName, contentType, conn));

        } catch (SQLException e) {
            sendErrorNotification(
                    NotificationGroup.COMPILER,
                    "Failed to read compiler result: {0}", e);
        }


        if (compilerMessages.size() == 0) {
            String contentDesc =
                    contentType == DBContentType.CODE_SPEC ? "spec of " :
                    contentType == DBContentType.CODE_BODY ? "body of " : "";

            String message = "The " + contentDesc + object.getQualifiedNameWithType() + " was " + (compilerAction.isSave() ? "updated" : "compiled") + " successfully.";
            CompilerMessage compilerMessage = new CompilerMessage(this, contentType, message);
            compilerMessages.add(compilerMessage);
        } else {
            Collections.sort(compilerMessages);
        }
    }

    private void loadCompilerErrors(ConnectionHandler connection, DBSchema schema, String objectName, DBContentType contentType, DBNConnection conn) throws SQLException {
        ResultSet resultSet = null;
        try {
            DatabaseMetadataInterface metadata = connection.getMetadataInterface();
            resultSet = metadata.loadCompileObjectErrors(
                    schema.getName(),
                    objectName,
                    conn);

            while (resultSet != null && resultSet.next()) {
                CompilerMessage errorMessage = new CompilerMessage(this, resultSet);
                error = true;
                if (/*!compilerAction.isDDL() || */contentType.isBundle() || contentType == errorMessage.getContentType()) {
                    compilerMessages.add(errorMessage);
                }
            }
        } finally {
            Resources.close(resultSet);
        }
    }

    public boolean isSingleMessage() {
        return compilerMessages.size() == 1;
    }

    @Nullable
    public DBSchemaObject getObject() {
        return DBObjectRef.get(object);
    }

    public ConnectionId getConnectionId() {
        return object.getConnectionId();
    }

    DBObjectType getObjectType() {
        return object.getObjectType();
    }

    public DBObjectRef<DBSchemaObject> getObjectRef() {
        return object;
    }

    @Override
    public void dispose() {
        compilerMessages.clear();
    }

    public Project getProject() {
        DBSchemaObject object = DBObjectRef.get(this.object);
        if (object == null) {
            ConnectionHandler connection = this.object.getConnection();
            if (connection != null) return connection.getProject();
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
