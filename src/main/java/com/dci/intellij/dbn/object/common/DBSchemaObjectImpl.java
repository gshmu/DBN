package com.dci.intellij.dbn.object.common;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.loader.DynamicContentResultSetLoader;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.Resources;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBObjectDependencyMetadata;
import com.dci.intellij.dbn.database.interfaces.DatabaseDataDefinitionInterface;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterfaceInvoker;
import com.dci.intellij.dbn.database.interfaces.DatabaseMetadataInterface;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.psql.PSQLLanguage;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.status.DBObjectStatusHolder;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBObjectVirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.dci.intellij.dbn.common.Priority.HIGHEST;
import static com.dci.intellij.dbn.common.content.DynamicContentProperty.DEPENDENCY;
import static com.dci.intellij.dbn.common.content.DynamicContentProperty.INTERNAL;
import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.*;
import static com.dci.intellij.dbn.object.type.DBObjectType.*;


public abstract class DBSchemaObjectImpl<M extends DBObjectMetadata> extends DBObjectImpl<M> implements DBSchemaObject {
    private DBObjectList<DBObject> referencedObjects;
    private DBObjectList<DBObject> referencingObjects;
    private volatile DBObjectStatusHolder objectStatus;

    public DBSchemaObjectImpl(@NotNull DBSchema schema, M metadata) throws SQLException {
        super(schema, metadata);
    }

    public DBSchemaObjectImpl(@NotNull DBSchemaObject parent, M metadata) throws SQLException {
        super(parent, metadata);
    }

    @Override
    protected void initProperties() {
        properties.set(EDITABLE, true);
        properties.set(REFERENCEABLE, true);
        properties.set(SCHEMA_OBJECT, true);
    }

    @Override
    protected void initLists() {
        if (is(REFERENCEABLE)) {
            DBObjectListContainer childObjects = ensureChildObjects();
            referencedObjects = childObjects.createObjectList(INCOMING_DEPENDENCY, this, INTERNAL, DEPENDENCY);
            referencingObjects = childObjects.createObjectList(OUTGOING_DEPENDENCY, this, INTERNAL, DEPENDENCY);
        }
    }

    @Override
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

    @Override
    public boolean isEditable(DBContentType contentType) {
        return false;
    }

    @Override
    public List<DBObject> getReferencedObjects() {
        return referencedObjects == null ? Collections.emptyList() : referencedObjects.getObjects();
    }

    @Override
    public List<DBObject> getReferencingObjects() {
        return referencingObjects == null ? Collections.emptyList() : referencingObjects.getObjects();
    }

    @Override
    public DBLanguage getCodeLanguage(DBContentType contentType) {
        return PSQLLanguage.INSTANCE;
    }

    @Override
    public String getCodeParseRootId(DBContentType contentType) {
        return null;
    }

    @Override
    @NotNull
    public DBObjectVirtualFile<?> getVirtualFile() {
        if (getObjectType().isSchemaObject()) {
            DatabaseFileSystem databaseFileSystem = DatabaseFileSystem.getInstance();
            return databaseFileSystem.findOrCreateDatabaseFile(getProject(), ref());
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
        return DatabaseInterfaceInvoker.load(HIGHEST,
                "Loading data dictionary",
                "Loading schema references for " + getQualifiedNameWithType(),
                getProject(),
                getConnectionId(),
                conn -> {
                    List<DBSchema> schemas = new ArrayList<>();
                    ResultSet resultSet = null;
                    try {
                        DBSchema schema = getSchema();
                        DatabaseMetadataInterface metadataInterface = getMetadataInterface();
                        resultSet = metadataInterface.loadReferencingSchemas(getSchemaName(), getName(), conn);
                        DBObjectBundle objectBundle = getObjectBundle();
                        while (resultSet.next()) {
                            String schemaName = resultSet.getString("SCHEMA_NAME");
                            DBSchema sch = objectBundle.getSchema(schemaName);
                            if (sch != null) {
                                schemas.add(sch);
                            }
                        }
                        if (schemas.isEmpty()) {
                            schemas.add(schema);
                        }

                    } finally {
                        Resources.close(resultSet);
                    }
                    return schemas;
                });
    }

    @Override
    public void executeUpdateDDL(DBContentType contentType, String oldCode, String newCode) throws SQLException {
        DatabaseInterfaceInvoker.execute(HIGHEST,
                "Updating source code",
                "Updating sources of " + getQualifiedNameWithType(),
                getProject(),
                getConnectionId(),
                getSchemaId(),
                conn -> {
                    ConnectionHandler connection = getConnection();
                    DatabaseDataDefinitionInterface dataDefinition = connection.getDataDefinitionInterface();
                    dataDefinition.updateObject(getName(), getObjectType().getName(), oldCode, newCode, conn);
                });
    }

    /*********************************************************
     *                         Loaders                       *
     *********************************************************/
    static {
        new DynamicContentResultSetLoader<DBObject, DBObjectDependencyMetadata>(null, INCOMING_DEPENDENCY, true, false) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBObject> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                DBSchemaObject schemaObject = dynamicContent.ensureParentEntity();
                return metadataInterface.loadReferencedObjects(schemaObject.getSchemaName(), schemaObject.getName(), connection);
            }

            @Override
            public DBObject createElement(DynamicContent<DBObject> content, DBObjectDependencyMetadata metadata, LoaderCache cache) throws SQLException {
                String objectOwner = metadata.getObjectOwner();
                String objectName = metadata.getObjectName();
                String objectTypeName = metadata.getObjectType();
                DBObjectType objectType = get(objectTypeName);
                if (objectType == PACKAGE_BODY) objectType = PACKAGE;
                if (objectType == TYPE_BODY) objectType = TYPE;

                DBSchema schema = (DBSchema) cache.getObject(objectOwner);

                if (schema == null) {
                    DBSchemaObject schemaObject = content.ensureParentEntity();
                    schema = schemaObject.getObjectBundle().getSchema(objectOwner);
                    cache.setObject(objectOwner,  schema);
                }

                return schema == null ? null : schema.getChildObject(objectType, objectName, (short) 0, true);
            }
        };

        new DynamicContentResultSetLoader<DBObject, DBObjectDependencyMetadata>(null, OUTGOING_DEPENDENCY, true, false) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBObject> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                DBSchemaObject schemaObject = dynamicContent.ensureParentEntity();
                return metadataInterface.loadReferencingObjects(schemaObject.getSchemaName(), schemaObject.getName(), connection);
            }

            @Override
            public DBObject createElement(DynamicContent<DBObject> content, DBObjectDependencyMetadata metadata, LoaderCache cache) throws SQLException {
                String objectOwner = metadata.getObjectOwner();
                String objectName = metadata.getObjectName();
                String objectTypeName = metadata.getObjectType();
                DBObjectType objectType = get(objectTypeName);
                if (objectType == PACKAGE_BODY) objectType = PACKAGE;
                if (objectType == TYPE_BODY) objectType = TYPE;

                DBSchema schema = (DBSchema) cache.getObject(objectOwner);
                if (schema == null) {
                    DBSchemaObject schemaObject = content.ensureParentEntity();
                    schema = schemaObject.getObjectBundle().getSchema(objectOwner);
                    cache.setObject(objectOwner,  schema);
                }
                return schema == null ? null : schema.getChildObject(objectType, objectName, (short) 0, true);
            }
        };
    }

}
