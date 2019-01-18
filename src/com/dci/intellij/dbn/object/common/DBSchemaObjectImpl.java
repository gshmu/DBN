package com.dci.intellij.dbn.object.common;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentStatus;
import com.dci.intellij.dbn.common.content.loader.DynamicContentResultSetLoader;
import com.dci.intellij.dbn.common.util.ChangeTimestamp;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseDDLInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.psql.PSQLLanguage;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.loader.DBObjectTimestampLoader;
import com.dci.intellij.dbn.object.common.status.DBObjectStatusHolder;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBObjectVirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.dci.intellij.dbn.object.common.DBObjectType.*;
import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.*;


public abstract class DBSchemaObjectImpl extends DBObjectImpl implements DBSchemaObject {
    private DBObjectList<DBObject> referencedObjects;
    private DBObjectList<DBObject> referencingObjects;
    private DBObjectStatusHolder objectStatus;

    public DBSchemaObjectImpl(@NotNull DBSchema schema, ResultSet resultSet) throws SQLException {
        super(schema, resultSet);
    }

    public DBSchemaObjectImpl(@NotNull DBSchemaObject parent, ResultSet resultSet) throws SQLException {
        super(parent, resultSet);
    }

    protected void initProperties() {
        properties.set(EDITABLE, true);
        properties.set(REFERENCEABLE, true);
        properties.set(SCHEMA_OBJECT, true);
    }

    @Override
    protected void initLists() {
        if (is(REFERENCEABLE)) {
            DBObjectListContainer childObjects = initChildObjects();
            referencedObjects = childObjects.createObjectList(INCOMING_DEPENDENCY, this, DynamicContentStatus.INTERNAL);
            referencingObjects = childObjects.createObjectList(OUTGOING_DEPENDENCY, this, DynamicContentStatus.INTERNAL);
        }
    }

    public DBObjectStatusHolder getStatus() {
        if (objectStatus == null) {
            synchronized (this) {
                if (objectStatus == null) {
                    objectStatus = new DBObjectStatusHolder(getContentType());
                }
            }
        }
        return objectStatus;
    }

    public boolean isEditable(DBContentType contentType) {
        return false;
    }

    public List<DBObject> getReferencedObjects() {
        return referencedObjects == null ? Collections.emptyList() : referencedObjects.getObjects();
    }

    public List<DBObject> getReferencingObjects() {
        return referencingObjects == null ? Collections.emptyList() : referencingObjects.getObjects();
    }

    protected List<DBObjectNavigationList> createNavigationLists() {
        return new ArrayList<>();
    }

    @NotNull
    public ChangeTimestamp loadChangeTimestamp(DBContentType contentType) throws SQLException {
        if (DatabaseFeature.OBJECT_CHANGE_TRACING.isSupported(this)) {
            Timestamp timestamp = getTimestampLoader(contentType).load(this);
            return new ChangeTimestamp(timestamp == null ? new Timestamp(System.currentTimeMillis()) : timestamp);
        }
        return new ChangeTimestamp(new Timestamp(System.currentTimeMillis()));
    }

    public DBObjectTimestampLoader getTimestampLoader(DBContentType contentType) {
        return new DBObjectTimestampLoader(getTypeName().toUpperCase());
    }

    public String loadCodeFromDatabase(DBContentType contentType) throws SQLException {
        return null;
    }

    public DBLanguage getCodeLanguage(DBContentType contentType) {
        return PSQLLanguage.INSTANCE;
    }

    public String getCodeParseRootId(DBContentType contentType) {
        return null;
    }

    @NotNull
    public DBObjectVirtualFile getVirtualFile() {
        if (getObjectType().isSchemaObject()) {
            return DatabaseFileSystem.getInstance().findOrCreateDatabaseFile(getProject(), objectRef);
        }
        return super.getVirtualFile();
    }

    @Override
    public DBEditableObjectVirtualFile getEditableVirtualFile() {
        if (getObjectType().isSchemaObject()) {
            return (DBEditableObjectVirtualFile) getVirtualFile();
        } else {
            return (DBEditableObjectVirtualFile) getParentObject().getVirtualFile();
        }
    }

    @Nullable
    @Override
    public DBEditableObjectVirtualFile getCachedVirtualFile() {
        return DatabaseFileSystem.getInstance().findDatabaseFile(this);
    }

    @Override
    public List<DBSchema> getReferencingSchemas() throws SQLException {
        List<DBSchema> schemas = new ArrayList<>();
        ConnectionHandler connectionHandler = getConnectionHandler();
        DBNConnection connection = connectionHandler.getPoolConnection(getSchema(), true);
        ResultSet resultSet = null;
        try {
            DatabaseMetadataInterface metadataInterface = connectionHandler.getInterfaceProvider().getMetadataInterface();
            resultSet = metadataInterface.loadReferencingSchemas(getSchema().getName(), getName(), connection);
            while (resultSet.next()) {
                String schemaName = resultSet.getString("SCHEMA_NAME");
                DBSchema schema = getConnectionHandler().getObjectBundle().getSchema(schemaName);
                if (schema != null)  {
                    schemas.add(schema);
                }
            }
            if (schemas.isEmpty()) {
                schemas.add(getSchema());
            }

        } finally {
            ConnectionUtil.close(resultSet);
            connectionHandler.freePoolConnection(connection);
        }
        return schemas;
    }

    public void executeUpdateDDL(DBContentType contentType, String oldCode, String newCode) throws SQLException {
        ConnectionHandler connectionHandler = getConnectionHandler();
        DBNConnection connection = connectionHandler.getPoolConnection(getSchema(), true);
        try {
            DatabaseDDLInterface ddlInterface = connectionHandler.getInterfaceProvider().getDDLInterface();
            ddlInterface.updateObject(getName(), getObjectType().getName(), oldCode,  newCode, connection);
        } finally {
            connectionHandler.freePoolConnection(connection);
        }
    }

    /*********************************************************
     *                         Loaders                       *
     *********************************************************/
    static {
        new DynamicContentResultSetLoader(null, INCOMING_DEPENDENCY) {
            public ResultSet createResultSet(DynamicContent dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
                DBSchemaObject schemaObject = (DBSchemaObject) dynamicContent.getParentElement();
                return metadataInterface.loadReferencedObjects(schemaObject.getSchema().getName(), schemaObject.getName(), connection);
            }

            public DBObject createElement(DynamicContent dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
                String objectOwner = resultSet.getString("OBJECT_OWNER");
                String objectName = resultSet.getString("OBJECT_NAME");
                String objectTypeName = resultSet.getString("OBJECT_TYPE");
                DBObjectType objectType = get(objectTypeName);
                if (objectType == PACKAGE_BODY) objectType = PACKAGE;
                if (objectType == TYPE_BODY) objectType = TYPE;

                DBSchema schema = (DBSchema) loaderCache.getObject(objectOwner);

                if (schema == null) {
                    DBSchemaObject schemaObject = (DBSchemaObject) dynamicContent.getParentElement();
                    ConnectionHandler connectionHandler = schemaObject.getConnectionHandler();
                    schema = connectionHandler.getObjectBundle().getSchema(objectOwner);
                    loaderCache.setObject(objectOwner,  schema);
                }

                return schema == null ? null : schema.getChildObject(objectType, objectName, 0, true);
            }
        };

        new DynamicContentResultSetLoader(null, OUTGOING_DEPENDENCY) {
            public ResultSet createResultSet(DynamicContent dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
                DBSchemaObject schemaObject = (DBSchemaObject) dynamicContent.getParentElement();
                return metadataInterface.loadReferencingObjects(schemaObject.getSchema().getName(), schemaObject.getName(), connection);
            }

            public DBObject createElement(DynamicContent dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
                String objectOwner = resultSet.getString("OBJECT_OWNER");
                String objectName = resultSet.getString("OBJECT_NAME");
                String objectTypeName = resultSet.getString("OBJECT_TYPE");
                DBObjectType objectType = get(objectTypeName);
                if (objectType == PACKAGE_BODY) objectType = PACKAGE;
                if (objectType == TYPE_BODY) objectType = TYPE;

                DBSchema schema = (DBSchema) loaderCache.getObject(objectOwner);
                if (schema == null) {
                    DBSchemaObject schemaObject = (DBSchemaObject) dynamicContent.getParentElement();
                    ConnectionHandler connectionHandler = schemaObject.getConnectionHandler();
                    schema = connectionHandler.getObjectBundle().getSchema(objectOwner);
                    loaderCache.setObject(objectOwner,  schema);
                }
                return schema == null ? null : schema.getChildObject(objectType, objectName, 0, true);
            }
        };
    }

}
