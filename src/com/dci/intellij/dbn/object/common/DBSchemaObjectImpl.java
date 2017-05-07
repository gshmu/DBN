package com.dci.intellij.dbn.object.common;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.content.loader.DynamicContentResultSetLoader;
import com.dci.intellij.dbn.common.util.ChangeTimestamp;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.database.DatabaseDDLInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.ddl.DDLFileType;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.psql.PSQLLanguage;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.loader.DBObjectTimestampLoader;
import com.dci.intellij.dbn.object.common.property.DBObjectProperties;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import com.dci.intellij.dbn.object.common.status.DBObjectStatusHolder;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.DBObjectVirtualFile;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.openapi.diagnostic.Logger;


public abstract class DBSchemaObjectImpl extends DBObjectImpl implements DBSchemaObject {
    private static final Logger LOGGER = LoggerFactory.createLogger();

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
        DBObjectProperties properties = getProperties();
        properties.set(DBObjectProperty.EDITABLE);
        properties.set(DBObjectProperty.REFERENCEABLE);
        properties.set(DBObjectProperty.SCHEMA_OBJECT);
    }

    @Override
    protected void initLists() {
        DBObjectProperties properties = getProperties();
        if (properties.is(DBObjectProperty.REFERENCEABLE)) {
            DBObjectListContainer childObjects = initChildObjects();
            referencedObjects = childObjects.createObjectList(DBObjectType.INCOMING_DEPENDENCY, this, REFERENCED_OBJECTS_LOADER, false, true);
            referencingObjects = childObjects.createObjectList(DBObjectType.OUTGOING_DEPENDENCY, this, REFERENCING_OBJECTS_LOADER, false, true);
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
        return referencedObjects == null ? Collections.<DBObject>emptyList() : referencedObjects.getObjects();
    }

    public List<DBObject> getReferencingObjects() {
        return referencingObjects == null ? Collections.<DBObject>emptyList() : referencingObjects.getObjects();
    }

    protected List<DBObjectNavigationList> createNavigationLists() {
        return new ArrayList<DBObjectNavigationList>();
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

    public DDLFileType getDDLFileType(DBContentType contentType) {
        return null;
    }

    public DDLFileType[] getDDLFileTypes() {
        return null;
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
        if (getParentObject() instanceof DBSchema) {
            return DatabaseFileSystem.getInstance().findOrCreateDatabaseFile(this);
        }
        return super.getVirtualFile();
    }

    @Override
    public DBEditableObjectVirtualFile getEditableVirtualFile() {
        DBObject parentObject = getParentObject();
        if (parentObject instanceof DBSchema) {
            return (DBEditableObjectVirtualFile) getVirtualFile();
        } else {
            return (DBEditableObjectVirtualFile) parentObject.getVirtualFile();
        }
    }

    @Nullable
    @Override
    public DBEditableObjectVirtualFile getCachedVirtualFile() {
        return DatabaseFileSystem.getInstance().findDatabaseFile(this);
    }

    @Override
    public List<DBSchema> getReferencingSchemas() throws SQLException {
        List<DBSchema> schemas = new ArrayList<DBSchema>();
        ConnectionHandler connectionHandler = getConnectionHandler();
        Connection connection = connectionHandler.getPoolConnection(getSchema(), true);
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
            ConnectionUtil.closeResultSet(resultSet);
            connectionHandler.freePoolConnection(connection);
        }
        return schemas;
    }

    public void executeUpdateDDL(DBContentType contentType, String oldCode, String newCode) throws SQLException {
        ConnectionHandler connectionHandler = getConnectionHandler();
        Connection connection = connectionHandler.getPoolConnection(getSchema(), true);
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
    private static final DynamicContentLoader REFERENCED_OBJECTS_LOADER = new DynamicContentResultSetLoader() {
        public ResultSet createResultSet(DynamicContent dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBSchemaObject schemaObject = (DBSchemaObject) dynamicContent.getParentElement();
            return metadataInterface.loadReferencedObjects(schemaObject.getSchema().getName(), schemaObject.getName(), connection);
        }

        public DBObject createElement(DynamicContent dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            String objectOwner = resultSet.getString("OBJECT_OWNER");
            String objectName = resultSet.getString("OBJECT_NAME");
            String objectTypeName = resultSet.getString("OBJECT_TYPE");
            DBObjectType objectType = DBObjectType.getObjectType(objectTypeName);
            if (objectType == DBObjectType.PACKAGE_BODY) objectType = DBObjectType.PACKAGE;
            if (objectType == DBObjectType.TYPE_BODY) objectType = DBObjectType.TYPE;

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

    private static final DynamicContentLoader REFERENCING_OBJECTS_LOADER = new DynamicContentResultSetLoader() {
        public ResultSet createResultSet(DynamicContent dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBSchemaObject schemaObject = (DBSchemaObject) dynamicContent.getParentElement();
            return metadataInterface.loadReferencingObjects(schemaObject.getSchema().getName(), schemaObject.getName(), connection);
        }

        public DBObject createElement(DynamicContent dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            String objectOwner = resultSet.getString("OBJECT_OWNER");
            String objectName = resultSet.getString("OBJECT_NAME");
            String objectTypeName = resultSet.getString("OBJECT_TYPE");
            DBObjectType objectType = DBObjectType.getObjectType(objectTypeName);
            if (objectType == DBObjectType.PACKAGE_BODY) objectType = DBObjectType.PACKAGE;
            if (objectType == DBObjectType.TYPE_BODY) objectType = DBObjectType.TYPE;

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
