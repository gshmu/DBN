package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.browser.model.BrowserTreeEventListener;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.loader.DynamicContentResultSetLoader;
import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ResourceUtil;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.database.common.metadata.def.DBArgumentMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBClusterMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBColumnMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBConstraintColumnMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBConstraintMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBDatabaseLinkMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBDimensionMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBFunctionMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBIndexColumnMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBIndexMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBMaterializedViewMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBNestedTableMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBPackageMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBProcedureMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBSchemaMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBSequenceMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBSynonymMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBTableMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBTriggerMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBTypeAttributeMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBTypeMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBViewMetadata;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.*;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectImpl;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectListVisitor;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationListImpl;
import com.dci.intellij.dbn.object.common.list.DBObjectRelationListContainer;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.object.common.status.DBObjectStatusHolder;
import com.dci.intellij.dbn.object.type.DBObjectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.dci.intellij.dbn.common.content.DynamicContentStatus.INTERNAL;
import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.*;
import static com.dci.intellij.dbn.object.type.DBObjectRelationType.CONSTRAINT_COLUMN;
import static com.dci.intellij.dbn.object.type.DBObjectRelationType.INDEX_COLUMN;
import static com.dci.intellij.dbn.object.type.DBObjectType.*;

public class DBSchemaImpl extends DBObjectImpl<DBSchemaMetadata> implements DBSchema {
    private DBObjectList<DBTable> tables;
    private DBObjectList<DBView> views;
    private DBObjectList<DBMaterializedView> materializedViews;
    private DBObjectList<DBSynonym> synonyms;
    private DBObjectList<DBSequence> sequences;
    private DBObjectList<DBProcedure> procedures;
    private DBObjectList<DBFunction> functions;
    private DBObjectList<DBPackage> packages;
    private DBObjectList<DBType> types;
    private DBObjectList<DBDatabaseTrigger> databaseTriggers;
    private DBObjectList<DBDimension> dimensions;
    private DBObjectList<DBCluster> clusters;
    private DBObjectList<DBDatabaseLink> databaseLinks;

    public DBSchemaImpl(ConnectionHandler connectionHandler, DBSchemaMetadata metadata) throws SQLException {
        super(connectionHandler, metadata);
    }

    @Override
    protected String initObject(DBSchemaMetadata metadata) throws SQLException {
        String name = metadata.getSchemaName();
        set(PUBLIC_SCHEMA, metadata.isPublic());
        set(SYSTEM_SCHEMA, metadata.isSystem());
        set(EMPTY_SCHEMA, metadata.isEmpty());
        set(USER_SCHEMA, name.equalsIgnoreCase(getConnectionHandler().getUserName()));
        return name;
    }

    @Override
    protected void initLists() {
        DBObjectListContainer ol = initChildObjects();
        DBObjectRelationListContainer orl = initChildObjectRelations();

        tables = ol.createObjectList(TABLE, this);
        views = ol.createObjectList(VIEW, this);
        materializedViews = ol.createObjectList(MATERIALIZED_VIEW, this);
        synonyms = ol.createObjectList(SYNONYM, this);
        sequences = ol.createObjectList(SEQUENCE, this);
        procedures = ol.createObjectList(PROCEDURE, this);
        functions = ol.createObjectList(FUNCTION, this);
        packages = ol.createObjectList(PACKAGE, this);
        types = ol.createObjectList(TYPE, this);
        databaseTriggers = ol.createObjectList(DATABASE_TRIGGER, this);
        dimensions = ol.createObjectList(DIMENSION, this);
        clusters = ol.createObjectList(CLUSTER, this);
        databaseLinks = ol.createObjectList(DBLINK, this);

        DBObjectList constraints = ol.createObjectList(CONSTRAINT, this, INTERNAL);
        DBObjectList indexes = ol.createObjectList(INDEX, this, INTERNAL);
        DBObjectList columns = ol.createObjectList(COLUMN, this, INTERNAL);
        ol.createObjectList(DATASET_TRIGGER, this, INTERNAL);
        ol.createObjectList(NESTED_TABLE, this, INTERNAL);
        ol.createObjectList(PACKAGE_FUNCTION, this, INTERNAL);
        ol.createObjectList(PACKAGE_PROCEDURE, this, INTERNAL);
        ol.createObjectList(PACKAGE_TYPE, this, INTERNAL);
        ol.createObjectList(TYPE_ATTRIBUTE, this, INTERNAL);
        ol.createObjectList(TYPE_FUNCTION, this, INTERNAL);
        ol.createObjectList(TYPE_PROCEDURE, this, INTERNAL);
        ol.createObjectList(ARGUMENT, this, INTERNAL);

        //ol.createHiddenObjectList(DBObjectType.TYPE_METHOD, this, TYPE_METHODS_LOADER);

        orl.createObjectRelationList(
                CONSTRAINT_COLUMN, this,
                constraints,
                columns);

        orl.createObjectRelationList(
                INDEX_COLUMN, this,
                indexes,
                columns);
    }

    @Override
    public void initProperties() {}

    @Nullable
    @Override
    public DBUser getOwner() {
        return getObjectBundle().getUser(getName());
    }

    @NotNull
    @Override
    public DBObjectType getObjectType() {
        return SCHEMA;
    }

    @Override
    public boolean isPublicSchema() {
        return is(PUBLIC_SCHEMA);
    }

    @Override
    public boolean isUserSchema() {
        return is(USER_SCHEMA);
    }

    @Override
    public boolean isSystemSchema() {
        return is(SYSTEM_SCHEMA);
    }

    @Override
    public boolean isEmptySchema() {
        return is(EMPTY_SCHEMA);
    }

    @Nullable
    @Override
    public DBObject getDefaultNavigationObject() {
        return getOwner();
    }

    @Override
    public DBObject getChildObject(DBObjectType objectType, String name, int overload, boolean lookupHidden) {
        if (objectType.isSchemaObject()) {
            DBObject object = super.getChildObject(objectType, name, overload, lookupHidden);
            if (object == null && objectType != SYNONYM) {
                DBSynonym synonym = (DBSynonym) super.getChildObject(SYNONYM, name, overload, lookupHidden);
                if (synonym != null) {
                    DBObject underlyingObject = synonym.getUnderlyingObject();
                    if (underlyingObject != null && underlyingObject.isOfType(objectType)) {
                        return synonym;
                    }
                }
            } else {
                return object;
            }
        }
        return null;
    }

    @Override
    protected List<DBObjectNavigationList> createNavigationLists() {
        DBUser user = getOwner();
        if (user != null) {
            List<DBObjectNavigationList> objectNavigationLists = new ArrayList<>();
            objectNavigationLists.add(new DBObjectNavigationListImpl("User", user));
            return objectNavigationLists;
        }
        return null;
    }

    private class ConstraintColumnComparator implements Comparator {
        private DBConstraint constraint;
        ConstraintColumnComparator(DBConstraint constraint) {
            this.constraint = constraint;
        }
        @Override
        public int compare(Object o1, Object o2) {
            DBColumn column1 = (DBColumn) o1;
            DBColumn column2 = (DBColumn) o2;
            return column1.getConstraintPosition(constraint)-
                    column2.getConstraintPosition(constraint);
        }
    }

    @Override
    public List<DBTable> getTables() {
        return tables.getObjects();
    }

    @Override
    public List<DBView> getViews() {
        return views.getObjects();
    }

    @Override
    public List<DBMaterializedView> getMaterializedViews() {
        return materializedViews.getObjects();
    }

    @Override
    public List<DBIndex> getIndexes() {
        return (List<DBIndex>) initChildObjects().getObjects(INDEX, true);
    }

    @Override
    public List<DBSynonym> getSynonyms() {
        return synonyms.getObjects();
    }

    @Override
    public List<DBSequence> getSequences() {
        return sequences.getObjects();
    }

    @Override
    public List<DBProcedure> getProcedures() {
        return procedures.getObjects();
    }

    @Override
    public List<DBFunction> getFunctions() {
        return functions.getObjects();
    }

    @Override
    public List<DBPackage> getPackages() {
        return packages.getObjects();
    }

    @Override
    public List<DBDatasetTrigger> getDatasetTriggers() {
        return (List<DBDatasetTrigger>) initChildObjects().getObjects(DATASET_TRIGGER, true);
    }

    @Override
    public List<DBDatabaseTrigger> getDatabaseTriggers() {
        return (List<DBDatabaseTrigger>) initChildObjects().getObjects(DATABASE_TRIGGER, false);
    }

    @Override
    public List<DBType> getTypes() {
        return types.getObjects();
    }

    @Override
    public List<DBDimension> getDimensions() {
        return dimensions.getObjects();
    }

    @Override
    public List<DBCluster> getClusters() {
        return clusters.getObjects();
    }

    @Override
    public List<DBDatabaseLink> getDatabaseLinks() {
        return databaseLinks.getObjects();
    }


    @Override
    public DBTable getTable(String name) {
        return tables.getObject(name);
    }

    @Override
    public DBView getView(String name) {
        return views.getObject(name);
    }

    @Override
    public DBMaterializedView getMaterializedView(String name) {
        return materializedViews.getObject(name);
    }

    @Override
    public DBIndex getIndex(String name) {
        DBObjectList indexList = initChildObjects().getObjectList(INDEX);
        return indexList == null ? null : (DBIndex) indexList.getObject(name);
    }

    @Override
    public DBCluster getCluster(String name) {
        return clusters.getObject(name);
    }

    @Override
    public DBDatabaseLink getDatabaseLink(String name) {
        return databaseLinks.getObject(name);
    }

    @Override
    public List<DBDataset> getDatasets() {
        List<DBDataset> datasets = new ArrayList<DBDataset>();
        datasets.addAll(getTables());
        datasets.addAll(getViews());
        datasets.addAll(getMaterializedViews());
        return datasets;
    }


    @Override
    public DBDataset getDataset(String name) {
        DBDataset dataset = getTable(name);
        if (dataset == null) {
            dataset = getView(name);
            if (dataset == null && DatabaseCompatibilityInterface.getInstance(this).supportsObjectType(MATERIALIZED_VIEW.getTypeId())) {
                dataset = getMaterializedView(name);
            }
        }
        if (dataset == null) {
            //System.out.println("unknown dataset: " + getName() + "." + name);
        }
        return dataset;
    }

    @Nullable
    private <T extends DBSchemaObject> T getObjectFallbackOnSynonym(DBObjectList<T> objects, String name) {
        T object = objects.getObject(name);
        if (object == null && DatabaseCompatibilityInterface.getInstance(this).supportsObjectType(SYNONYM.getTypeId())) {
            DBSynonym synonym = synonyms.getObject(name);
            if (synonym != null) {
                DBObject underlyingObject = synonym.getUnderlyingObject();
                if (underlyingObject != null) {
                    if (underlyingObject.getObjectType() == objects.getObjectType()) {
                        return (T) underlyingObject;
                    }
                }
            }
        } else {
            return object;
        }
        return null;
    }

    @Override
    public DBType getType(String name) {
        return getObjectFallbackOnSynonym(types, name);
    }

    @Override
    public DBPackage getPackage(String name) {
        return getObjectFallbackOnSynonym(packages, name);
    }

    @Override
    public DBProcedure getProcedure(String name, int overload) {
        return overload > 0 ?
                procedures.getObject(name, overload) :
                getObjectFallbackOnSynonym(procedures, name);
    }

    @Override
    public DBFunction getFunction(String name, int overload) {
        return overload > 0 ?
                functions.getObject(name, overload) :
                getObjectFallbackOnSynonym(functions, name);
    }

    @Override
    public DBProgram getProgram(String name) {
        DBProgram program = getPackage(name);
        if (program == null) program = getType(name);
        return program;
    }

    @Override
    public DBMethod getMethod(String name, DBObjectType methodType, int overload) {
        if (methodType == null) {
            DBMethod method = getProcedure(name, overload);
            if (method == null) method = getFunction(name, overload);
            return method;
        } else if (methodType == PROCEDURE) {
            return getProcedure(name, overload);
        } else if (methodType == FUNCTION) {
            return getFunction(name, overload);
        }
        return null;
    }

    @Override
    public DBMethod getMethod(String name, int overload) {
        return getMethod(name, null, overload);
    }

    @Override
    public boolean isParentOf(DBObject object) {
        if (object instanceof DBSchemaObject) {
            DBSchemaObject schemaObject = (DBSchemaObject) object;
            return schemaObject.is(SCHEMA_OBJECT) && this.equals(schemaObject.getSchema());

        }
        return false;
    }

    @Override
    public synchronized void refreshObjectsStatus() throws SQLException {
        Set<BrowserTreeNode> refreshNodes = resetObjectsStatus();
        DBNConnection connection = null;
        ResultSet resultSet = null;
        ConnectionHandler connectionHandler = getConnectionHandler();
        try {
            connection = connectionHandler.getPoolConnection(true);
            DatabaseMetadataInterface metadataInterface = connectionHandler.getInterfaceProvider().getMetadataInterface();
            try {
                resultSet = metadataInterface.loadInvalidObjects(getName(), connection);
                while (resultSet != null && resultSet.next()) {
                    String objectName = resultSet.getString("OBJECT_NAME");
                    DBSchemaObject schemaObject = (DBSchemaObject) getChildObjectNoLoad(objectName);
                    if (schemaObject != null && schemaObject.is(INVALIDABLE)) {
                        DBObjectStatusHolder objectStatus = schemaObject.getStatus();
                        boolean statusChanged;

                        if (schemaObject.getContentType().isBundle()) {
                            String objectType = resultSet.getString("OBJECT_TYPE");
                            statusChanged = objectType.contains("BODY") ?
                                    objectStatus.set(DBContentType.CODE_BODY, DBObjectStatus.VALID, false) :
                                    objectStatus.set(DBContentType.CODE_SPEC, DBObjectStatus.VALID, false);
                        }
                        else {
                            statusChanged = objectStatus.set(DBObjectStatus.VALID, false);
                        }
                        if (statusChanged) {
                            refreshNodes.add(schemaObject.getParent());
                        }
                    }
                }
            }
            finally {
                ResourceUtil.close(resultSet);
            }

            try {
                resultSet = metadataInterface.loadDebugObjects(getName(), connection);
                while (resultSet != null && resultSet.next()) {
                    String objectName = resultSet.getString("OBJECT_NAME");
                    DBSchemaObject schemaObject = (DBSchemaObject) getChildObjectNoLoad(objectName);
                    if (schemaObject != null && schemaObject.is(DEBUGABLE)) {
                        DBObjectStatusHolder objectStatus = schemaObject.getStatus();
                        boolean statusChanged;

                        if (schemaObject.getContentType().isBundle()) {
                            String objectType = resultSet.getString("OBJECT_TYPE");
                            statusChanged = objectType.contains("BODY") ?
                                    objectStatus.set(DBContentType.CODE_BODY, DBObjectStatus.DEBUG, true) :
                                    objectStatus.set(DBContentType.CODE_SPEC, DBObjectStatus.DEBUG, true);
                        }
                        else {
                            statusChanged = objectStatus.set(DBObjectStatus.DEBUG, true);
                        }
                        if (statusChanged) {
                            refreshNodes.add(schemaObject.getParent());
                        }
                    }
                }
            } finally {
                ResourceUtil.close(resultSet);
            }

        } finally {
            connectionHandler.freePoolConnection(connection);
        }

        for (BrowserTreeNode treeNode : refreshNodes) {
            EventUtil.notify(getProject(),
                    BrowserTreeEventListener.TOPIC,
                    (listener) -> listener.nodeChanged(treeNode, TreeEventType.NODES_CHANGED));
        }

    }

    @Override
    public SchemaId getIdentifier() {
        return SchemaId.get(getName());
    }

    private Set<BrowserTreeNode> resetObjectsStatus() {
        ObjectStatusUpdater updater = new ObjectStatusUpdater();
        initChildObjects().visitLists(updater, true);
        return updater.getRefreshNodes();
    }

    class ObjectStatusUpdater extends DisposableBase implements DBObjectListVisitor {
        private Set<BrowserTreeNode> refreshNodes = new HashSet<BrowserTreeNode>();

        @Override
        public void visitObjectList(DBObjectList<DBObject> objectList) {
            if (objectList.isLoaded() && !objectList.isDirty() && !objectList.isLoading()) {
                List<DBObject> objects = objectList.getObjects();
                for (DBObject object : objects) {
                    checkDisposed();
                    ProgressMonitor.checkCancelled();

                    if (object instanceof DBSchemaObject) {
                        DBSchemaObject schemaObject = (DBSchemaObject) object;
                        DBObjectStatusHolder objectStatus = schemaObject.getStatus();
                        if (schemaObject.is(INVALIDABLE)) {
                            if (objectStatus.set(DBObjectStatus.VALID, true)) {
                                refreshNodes.add(object.getParent());
                            }
                        }
                        if (schemaObject.is(DEBUGABLE)) {
                            if (objectStatus.set(DBObjectStatus.DEBUG, false)) {
                                refreshNodes.add(object.getParent());
                            }
                        }
                    } else {
                        break;
                    }
                }
            }
        }


        public Set<BrowserTreeNode> getRefreshNodes() {
            return refreshNodes;
        }
    }

    @Override
    public void buildToolTip(HtmlToolTipBuilder ttb) {
        ttb.append(true, getObjectType().getName(), true);
        ttb.createEmptyRow();
        super.buildToolTip(ttb);
    }

    /*********************************************************
     *                     TreeElement                       *
     *********************************************************/
    @Override
    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return DatabaseBrowserUtils.createList(
                tables,
                views,
                materializedViews,
                synonyms,
                sequences,
                procedures,
                functions,
                packages,
                types,
                databaseTriggers,
                dimensions,
                clusters,
                databaseLinks);
    }

    /*********************************************************
     *                         Loaders                       *
     *********************************************************/
    static {
        new DynamicContentResultSetLoader<DBTable, DBTableMetadata>(SCHEMA, TABLE, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBTable> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadTables(schema.getName(), connection);
            }

            @Override
            public DBTable createElement(DynamicContent<DBTable> content, DBTableMetadata metadata, LoaderCache cache) throws SQLException {
                DBSchema schema = (DBSchema) content.getParentElement();
                return new DBTableImpl(schema, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBView, DBViewMetadata>(SCHEMA, VIEW, true, true){
            @Override
            public ResultSet createResultSet(DynamicContent<DBView> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadViews(schema.getName(), connection);
            }

            @Override
            public DBView createElement(DynamicContent<DBView> content, DBViewMetadata metadata, LoaderCache cache) throws SQLException {
                DBSchema schema = (DBSchema) content.getParentElement();
                return new DBViewImpl(schema, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBMaterializedView, DBMaterializedViewMetadata>(SCHEMA, MATERIALIZED_VIEW, true, true){
            @Override
            public ResultSet createResultSet(DynamicContent<DBMaterializedView> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadMaterializedViews(schema.getName(), connection);
            }

            @Override
            public DBMaterializedView createElement(DynamicContent<DBMaterializedView> content, DBMaterializedViewMetadata metadata, LoaderCache cache) throws SQLException {
                DBSchema schema = (DBSchema) content.getParentElement();
                return new DBMaterializedViewImpl(schema, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBSynonym, DBSynonymMetadata>(SCHEMA, SYNONYM, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBSynonym> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadSynonyms(schema.getName(), connection);
            }

            @Override
            public DBSynonym createElement(DynamicContent<DBSynonym> content, DBSynonymMetadata metadata, LoaderCache cache) throws SQLException {
                DBSchema schema = (DBSchema) content.getParentElement();
                return new DBSynonymImpl(schema, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBSequence, DBSequenceMetadata>(SCHEMA, SEQUENCE, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBSequence> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadSequences(schema.getName(), connection);
            }

            @Override
            public DBSequence createElement(DynamicContent<DBSequence> content, DBSequenceMetadata metadata, LoaderCache cache) throws SQLException {
                DBSchema schema = (DBSchema) content.getParentElement();
                return new DBSequenceImpl(schema, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBProcedure, DBProcedureMetadata>(SCHEMA, PROCEDURE, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBProcedure> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadProcedures(schema.getName(), connection);
            }

            @Override
            public DBProcedure createElement(DynamicContent<DBProcedure> content, DBProcedureMetadata metadata, LoaderCache cache) throws SQLException {
                DBSchema schema = (DBSchema) content.getParentElement();
                return new DBProcedureImpl(schema, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBFunction, DBFunctionMetadata>(SCHEMA, FUNCTION, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBFunction> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadFunctions(schema.getName(), connection);
            }
            @Override
            public DBFunction createElement(DynamicContent<DBFunction> content, DBFunctionMetadata metadata, LoaderCache cache) throws SQLException {
                DBSchema schema = (DBSchema) content.getParentElement();
                return new DBFunctionImpl(schema, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBPackage, DBPackageMetadata>(SCHEMA, PACKAGE, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBPackage> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadPackages(schema.getName(), connection);
            }

            @Override
            public DBPackage createElement(DynamicContent<DBPackage> content, DBPackageMetadata metadata, LoaderCache cache) throws SQLException {
                DBSchema schema = (DBSchema) content.getParentElement();
                return new DBPackageImpl(schema, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBType, DBTypeMetadata>(SCHEMA, TYPE, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBType> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadTypes(schema.getName(), connection);
            }

            @Override
            public DBType createElement(DynamicContent<DBType> content, DBTypeMetadata metadata, LoaderCache cache) throws SQLException {
                DBSchema schema = (DBSchema) content.getParentElement();
                return new DBTypeImpl(schema, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBDatabaseTrigger, DBTriggerMetadata>(SCHEMA, DATABASE_TRIGGER, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBDatabaseTrigger> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadDatabaseTriggers(schema.getName(), connection);
            }

            @Override
            public DBDatabaseTrigger createElement(DynamicContent<DBDatabaseTrigger> content, DBTriggerMetadata metadata, LoaderCache cache) throws SQLException {
                DBSchema schema = (DBSchema) content.getParentElement();
                return new DBDatabaseTriggerImpl(schema, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBDimension, DBDimensionMetadata>(SCHEMA, DIMENSION, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBDimension> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadDimensions(schema.getName(), connection);
            }

            @Override
            public DBDimension createElement(DynamicContent<DBDimension> content, DBDimensionMetadata metadata, LoaderCache cache) throws SQLException {
                DBSchema schema = (DBSchema) content.getParentElement();
                return new DBDimensionImpl(schema, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBCluster, DBClusterMetadata>(SCHEMA, CLUSTER, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBCluster> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadClusters(schema.getName(), connection);
            }

            @Override
            public DBCluster createElement(DynamicContent<DBCluster> content, DBClusterMetadata metadata, LoaderCache cache) throws SQLException {
                DBSchema schema = (DBSchema) content.getParentElement();
                return new DBClusterImpl(schema, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBDatabaseLink, DBDatabaseLinkMetadata>(SCHEMA, DBLINK, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadDatabaseLinks(schema.getName(), connection);
            }

            @Override
            public DBDatabaseLink createElement(DynamicContent<DBDatabaseLink> content, DBDatabaseLinkMetadata metadata, LoaderCache cache) throws SQLException {
                DBSchema schema = (DBSchema) content.getParentElement();
                return new DBDatabaseLinkImpl(schema, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBColumn, DBColumnMetadata>(SCHEMA, COLUMN, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBColumn> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadAllColumns(schema.getName(), connection);
            }

            @Override
            public DBColumn createElement(DynamicContent<DBColumn> content, DBColumnMetadata provider, LoaderCache cache) throws SQLException {
                String datasetName = provider.getDatasetName();

                DBDataset dataset = (DBDataset) cache.getObject(datasetName);
                if (dataset == null) {
                    DBSchema schema = (DBSchema) content.getParentElement();
                    dataset = schema.getDataset(datasetName);
                    cache.setObject(datasetName, dataset);
                }

                // dataset may be null if cluster column!!
                return dataset == null ? null : new DBColumnImpl(dataset, provider);
            }
        };

        new DynamicContentResultSetLoader<DBConstraint, DBConstraintMetadata>(SCHEMA, CONSTRAINT, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBConstraint> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadAllConstraints(schema.getName(), connection);
            }

            @Override
            public DBConstraint createElement(DynamicContent<DBConstraint> content, DBConstraintMetadata metadata, LoaderCache cache) throws SQLException {
                String datasetName = metadata.getDatasetName();

                DBDataset dataset = (DBDataset) cache.getObject(datasetName);
                if (dataset == null) {
                    DBSchema schema = (DBSchema) content.getParentElement();
                    dataset = schema.getDataset(datasetName);
                    cache.setObject(datasetName, dataset);
                }

                return dataset == null ? null : new DBConstraintImpl(dataset, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBIndex, DBIndexMetadata>(SCHEMA, INDEX, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBIndex> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadAllIndexes(schema.getName(), connection);
            }

            @Override
            public DBIndex createElement(DynamicContent<DBIndex> content, DBIndexMetadata metadata, LoaderCache cache) throws SQLException {
                String datasetName = metadata.getTableName();

                DBDataset dataset = (DBDataset) cache.getObject(datasetName);
                if (dataset == null) {
                    DBSchema schema = (DBSchema) content.getParentElement();
                    dataset = schema.getDataset(datasetName);
                    cache.setObject(datasetName, dataset);
                }

                return dataset == null ? null : new DBIndexImpl(dataset, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBDatasetTrigger, DBTriggerMetadata>(SCHEMA, DATASET_TRIGGER, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBDatasetTrigger> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadAllDatasetTriggers(schema.getName(), connection);
            }

            @Override
            public DBDatasetTrigger createElement(DynamicContent<DBDatasetTrigger> content, DBTriggerMetadata metadata, LoaderCache cache) throws SQLException {
                String datasetName = metadata.getDatasetName();
                DBDataset dataset = (DBDataset) cache.getObject(datasetName);
                if (dataset == null) {
                    DBSchema schema = (DBSchema) content.getParentElement();
                    dataset = schema.getDataset(datasetName);
                    cache.setObject(datasetName, dataset);
                }
                return dataset == null ? null : new DBDatasetTriggerImpl(dataset, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBNestedTable, DBNestedTableMetadata>(SCHEMA, NESTED_TABLE, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadAllNestedTables(schema.getName(), connection);
            }

            @Override
            public DBNestedTable createElement(DynamicContent<DBNestedTable> content, DBNestedTableMetadata metadata, LoaderCache cache) throws SQLException {
                String tableName = metadata.getTableName();
                DBTable table = (DBTable) cache.getObject(tableName);
                if (table == null) {
                    DBSchema schema = (DBSchema) content.getParentElement();
                    table = schema.getTable(tableName);
                    cache.setObject(tableName, table);
                }
                return table == null ? null : new DBNestedTableImpl(table, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBPackageFunction, DBFunctionMetadata>(SCHEMA, PACKAGE_FUNCTION, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadAllPackageFunctions(schema.getName(), connection);
            }

            @Override
            public DBPackageFunction createElement(DynamicContent<DBPackageFunction> content, DBFunctionMetadata metadata, LoaderCache cache) throws SQLException {
                String packageName = metadata.getPackageName();
                DBPackage packagee = (DBPackage) cache.getObject(packageName);
                if (packagee == null) {
                    DBSchema schema = (DBSchema) content.getParentElement();
                    packagee = schema.getPackage(packageName);
                    cache.setObject(packageName, packagee);
                }
                return packagee == null ? null : new DBPackageFunctionImpl(packagee, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBPackageProcedure, DBProcedureMetadata>(SCHEMA, PACKAGE_PROCEDURE, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadAllPackageProcedures(schema.getName(), connection);
            }

            @Override
            public DBPackageProcedure createElement(DynamicContent<DBPackageProcedure> content, DBProcedureMetadata metadata, LoaderCache cache) throws SQLException {
                String packageName = metadata.getPackageName();
                DBPackage packagee = (DBPackage) cache.getObject(packageName);
                if (packagee == null) {
                    DBSchema schema = (DBSchema) content.getParentElement();
                    packagee = schema.getPackage(packageName);
                    cache.setObject(packageName, packagee);
                }
                return packagee == null ? null : new DBPackageProcedureImpl(packagee, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBPackageType, DBTypeMetadata>(SCHEMA, PACKAGE_TYPE, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBPackageType> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadAllPackageTypes(schema.getName(), connection);
            }

            @Override
            public DBPackageType createElement(DynamicContent<DBPackageType> content, DBTypeMetadata metadata, LoaderCache cache) throws SQLException {
                String packageName = metadata.getPackageName();
                DBPackage packagee = (DBPackage) cache.getObject(packageName);
                if (packagee == null) {
                    DBSchema schema = (DBSchema) content.getParentElement();
                    packagee = schema.getPackage(packageName);
                    cache.setObject(packageName, packagee);
                }
                return packagee == null ? null : new DBPackageTypeImpl(packagee, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBTypeAttribute, DBTypeAttributeMetadata>(SCHEMA, TYPE_ATTRIBUTE, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBTypeAttribute> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadAllTypeAttributes(schema.getName(), connection);
            }

            @Override
            public DBTypeAttribute createElement(DynamicContent<DBTypeAttribute> content, DBTypeAttributeMetadata provider, LoaderCache cache) throws SQLException {
                String typeName = provider.getTypeName();
                DBType type = (DBType) cache.getObject(typeName);
                if (type == null) {
                    DBSchema schema = (DBSchema) content.getParentElement();
                    type = schema.getType(typeName);
                    cache.setObject(typeName, type);
                }
                return type == null ? null : new DBTypeAttributeImpl(type, provider);
            }
        };

        new DynamicContentResultSetLoader<DBTypeFunction, DBFunctionMetadata>(SCHEMA, TYPE_FUNCTION, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadAllTypeFunctions(schema.getName(), connection);
            }

            @Override
            public DBTypeFunction createElement(DynamicContent<DBTypeFunction> content, DBFunctionMetadata metadata, LoaderCache cache) throws SQLException {
                String typeName = metadata.getTypeName();
                DBType type = (DBType) cache.getObject(typeName);
                if (type == null) {
                    DBSchema schema = (DBSchema) content.getParentElement();
                    type = schema.getType(typeName);
                    cache.setObject(typeName, type);
                }
                return type == null ?  null : new DBTypeFunctionImpl(type, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBTypeProcedure, DBProcedureMetadata>(SCHEMA, TYPE_PROCEDURE, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadAllTypeProcedures(schema.getName(), connection);
            }

            @Override
            public DBTypeProcedure createElement(DynamicContent<DBTypeProcedure> content, DBProcedureMetadata metadata, LoaderCache cache) throws SQLException {
                String typeName = metadata.getTypeName();
                DBType type = (DBType) cache.getObject(typeName);
                if (type == null) {
                    DBSchema schema = (DBSchema) content.getParentElement();
                    type = schema.getType(typeName);
                    cache.setObject(typeName, type);
                }
                return type == null ? null : new DBTypeProcedureImpl(type, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBArgument, DBArgumentMetadata>(SCHEMA, ARGUMENT, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBArgument> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadAllMethodArguments(schema.getName(), connection);
            }

            @Override
            public DBArgument createElement(DynamicContent<DBArgument> content, DBArgumentMetadata metadata, LoaderCache cache) throws SQLException {
                String programName = metadata.getProgramName();
                String methodName = metadata.getMethodName();
                String methodType = metadata.getMethodType();
                int overload = metadata.getOverload();
                DBSchema schema = (DBSchema) content.getParentElement();
                DBProgram program = programName == null ? null : schema.getProgram(programName);

                String cacheKey = methodName + methodType + overload;
                DBMethod method = (DBMethod) cache.getObject(cacheKey);
                DBObjectType objectType = get(methodType);

                if (method == null || method.getProgram() != program || method.getOverload() != overload) {
                    if (programName == null) {
                        method = schema.getMethod(methodName, objectType, overload);
                    } else {
                        method = program == null ? null : program.getMethod(methodName, overload);
                    }
                    cache.setObject(cacheKey, method);
                }
                return method == null ? null : new DBArgumentImpl(method, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBConstraintColumnRelation, DBConstraintColumnMetadata>(SCHEMA, CONSTRAINT_COLUMN, true, false) {
            @Override
            public ResultSet createResultSet(DynamicContent dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadAllConstraintRelations(schema.getName(), connection);
            }

            @Override
            public DBConstraintColumnRelation createElement(DynamicContent<DBConstraintColumnRelation> content, DBConstraintColumnMetadata metadata, LoaderCache cache) throws SQLException {
                String datasetName = metadata.getDatasetName();
                String columnName = metadata.getColumnName();
                String constraintName = metadata.getConstraintName();
                int position = metadata.getPosition();

/*
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            DBObjectList<DBConstraint> constraints = schema.getObjectLists().getObjectList(DBObjectType.CONSTRAINT);
            DBConstraint constraint = constraints.getObject(constraintName, datasetName);

            if (constraint != null) {
                DBObjectList<DBColumn> columns = schema.getObjectLists().getHiddenObjectList(DBObjectType.COLUMN);
                DBColumn column = columns.getObject(columnName, datasetName);

                if (column != null) {
                    return new DBConstraintColumnRelation(constraint, column, position);
                }
            }
*/
                DBDataset dataset = (DBDataset) cache.getObject(datasetName);
                if (dataset == null) {
                    DBSchema schema = (DBSchema) content.getParentElement();
                    dataset = schema.getDataset(datasetName);
                    cache.setObject(datasetName, dataset);
                }

                if (dataset != null) {
                    DBConstraint constraint = dataset.getConstraint(constraintName);
                    DBColumn column = dataset.getColumn(columnName);
                    if (column != null && constraint != null) {
                        return new DBConstraintColumnRelation(constraint, column, position);
                    }
                }

                return null;
            }
        };

        new DynamicContentResultSetLoader<DBIndexColumnRelation, DBIndexColumnMetadata>(SCHEMA, INDEX_COLUMN, true, false) {
            @Override
            public ResultSet createResultSet(DynamicContent dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadAllIndexRelations(schema.getName(), connection);
            }

            @Override
            public DBIndexColumnRelation createElement(DynamicContent<DBIndexColumnRelation> content, DBIndexColumnMetadata metadata, LoaderCache cache) throws SQLException {
                String datasetName = metadata.getTableName();
                String columnName = metadata.getColumnName();
                String indexName = metadata.getIndexName();

            /*DBSchema schema = (DBSchema) dynamicContent.getParent();
            DBObjectList<DBIndex> indexes = schema.getObjectLists().getObjectList(DBObjectType.INDEX);
            DBIndex index = indexes.getObject(indexName, tableName);

            if (index != null) {
                DBObjectList<DBColumn> columns = schema.getObjectLists().getHiddenObjectList(DBObjectType.COLUMN);
                DBColumn column = columns.getObject(columnName, tableName);

                if (column != null) {
                    return new DBIndexColumnRelation(index, column);
                }
            }*/

                DBDataset dataset = (DBDataset) cache.getObject(datasetName);
                if (dataset == null) {
                    DBSchema schema = (DBSchema) content.getParentElement();
                    dataset = schema.getDataset(datasetName);
                    cache.setObject(datasetName, dataset);
                }

                if (dataset != null) {
                    DBIndex index = dataset.getIndex(indexName);
                    DBColumn column = dataset.getColumn(columnName);

                    if (column != null && index != null) {
                        return new DBIndexColumnRelation(index, column);
                    }
                }
                return null;
            }
        };
    }
}
