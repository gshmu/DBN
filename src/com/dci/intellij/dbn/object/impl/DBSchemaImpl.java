package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.browser.model.BrowserTreeEventListener;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentElement;
import com.dci.intellij.dbn.common.content.loader.DynamicContentResultSetLoader;
import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.*;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectImpl;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectListVisitor;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationListImpl;
import com.dci.intellij.dbn.object.common.list.DBObjectRelation;
import com.dci.intellij.dbn.object.common.list.DBObjectRelationListContainer;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.object.common.status.DBObjectStatusHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.dci.intellij.dbn.common.content.DynamicContentStatus.INDEXED;
import static com.dci.intellij.dbn.common.content.DynamicContentStatus.INTERNAL;
import static com.dci.intellij.dbn.object.common.DBObjectRelationType.CONSTRAINT_COLUMN;
import static com.dci.intellij.dbn.object.common.DBObjectRelationType.INDEX_COLUMN;
import static com.dci.intellij.dbn.object.common.DBObjectType.*;
import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.*;

public class DBSchemaImpl extends DBObjectImpl implements DBSchema {
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

    public DBSchemaImpl(ConnectionHandler connectionHandler, ResultSet resultSet) throws SQLException {
        super(connectionHandler, resultSet);
    }

    @Override
    protected String initObject(ResultSet resultSet) throws SQLException {
        String name = resultSet.getString("SCHEMA_NAME");
        set(PUBLIC_SCHEMA, resultSet.getString("IS_PUBLIC").equals("Y"));
        set(SYSTEM_SCHEMA, resultSet.getString("IS_SYSTEM").equals("Y"));
        set(EMPTY_SCHEMA, resultSet.getString("IS_EMPTY").equals("Y"));
        set(USER_SCHEMA, name.equalsIgnoreCase(getConnectionHandler().getUserName()));
        return name;
    }

    @Override
    protected void initLists() {
        DBObjectListContainer ol = initChildObjects();
        DBObjectRelationListContainer orl = initChildObjectRelations();

        tables = ol.createObjectList(TABLE, this, INDEXED);
        views = ol.createObjectList(VIEW, this, INDEXED);
        materializedViews = ol.createObjectList(MATERIALIZED_VIEW, this, INDEXED);
        synonyms = ol.createObjectList(SYNONYM, this, INDEXED);
        sequences = ol.createObjectList(SEQUENCE, this, INDEXED);
        procedures = ol.createObjectList(PROCEDURE, this, INDEXED);
        functions = ol.createObjectList(FUNCTION, this, INDEXED);
        packages = ol.createObjectList(PACKAGE, this, INDEXED);
        types = ol.createObjectList(TYPE, this, INDEXED);
        databaseTriggers = ol.createObjectList(DATABASE_TRIGGER, this, INDEXED);
        dimensions = ol.createObjectList(DIMENSION, this, INDEXED);
        clusters = ol.createObjectList(CLUSTER, this, INDEXED);
        databaseLinks = ol.createObjectList(DBLINK, this, INDEXED);

        DBObjectList constraints = ol.createObjectList(CONSTRAINT, this, INDEXED, INTERNAL);
        DBObjectList indexes = ol.createObjectList(INDEX, this, INDEXED, INTERNAL);
        DBObjectList columns = ol.createObjectList(COLUMN, this, INTERNAL);
        ol.createObjectList(DATASET_TRIGGER, this, INDEXED, INTERNAL);
        ol.createObjectList(NESTED_TABLE, this, INDEXED, INTERNAL);
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

    public DBObjectType getObjectType() {
        return SCHEMA;
    }

    public boolean isPublicSchema() {
        return is(PUBLIC_SCHEMA);
    }

    public boolean isUserSchema() {
        return is(USER_SCHEMA);
    }

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
        public int compare(Object o1, Object o2) {
            DBColumn column1 = (DBColumn) o1;
            DBColumn column2 = (DBColumn) o2;
            return column1.getConstraintPosition(constraint)-
                    column2.getConstraintPosition(constraint);
        }
    }

    public List<DBTable> getTables() {
        return tables.getObjects();
    }

    public List<DBView> getViews() {
        return views.getObjects();
    }

    public List<DBMaterializedView> getMaterializedViews() {
        return materializedViews.getObjects();
    }

    public List<DBIndex> getIndexes() {
        return (List<DBIndex>) initChildObjects().getObjects(INDEX, true);
    }

    public List<DBSynonym> getSynonyms() {
        return synonyms.getObjects();
    }

    public List<DBSequence> getSequences() {
        return sequences.getObjects();
    }

    public List<DBProcedure> getProcedures() {
        return procedures.getObjects();
    }

    public List<DBFunction> getFunctions() {
        return functions.getObjects();
    }

    public List<DBPackage> getPackages() {
        return packages.getObjects();
    }

    public List<DBDatasetTrigger> getDatasetTriggers() {
        return (List<DBDatasetTrigger>) initChildObjects().getObjects(DATASET_TRIGGER, true);
    }

    public List<DBDatabaseTrigger> getDatabaseTriggers() {
        return (List<DBDatabaseTrigger>) initChildObjects().getObjects(DATABASE_TRIGGER, false);
    }

    public List<DBType> getTypes() {
        return types.getObjects();
    }

    public List<DBDimension> getDimensions() {
        return dimensions.getObjects();
    }

    public List<DBCluster> getClusters() {
        return clusters.getObjects();
    }

    public List<DBDatabaseLink> getDatabaseLinks() {
        return databaseLinks.getObjects();
    }


    public DBTable getTable(String name) {
        return tables.getObject(name);
    }

    public DBView getView(String name) {
        return views.getObject(name);
    }

    public DBMaterializedView getMaterializedView(String name) {
        return materializedViews.getObject(name);
    }

    public DBIndex getIndex(String name) {
        return (DBIndex) initChildObjects().getObjectList(INDEX).getObject(name);
    }

    public DBCluster getCluster(String name) {
        return clusters.getObject(name);
    }

    public DBDatabaseLink getDatabaseLink(String name) {
        return databaseLinks.getObject(name);
    }

    public List<DBDataset> getDatasets() {
        List<DBDataset> datasets = new ArrayList<DBDataset>();
        datasets.addAll(getTables());
        datasets.addAll(getViews());
        datasets.addAll(getMaterializedViews());
        return datasets;
    }


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

    public DBType getType(String name) {
        return getObjectFallbackOnSynonym(types, name);
    }

    public DBPackage getPackage(String name) {
        return getObjectFallbackOnSynonym(packages, name);
    }

    public DBProcedure getProcedure(String name, int overload) {
        return overload > 0 ?
                procedures.getObject(name, overload) :
                getObjectFallbackOnSynonym(procedures, name);
    }

    public DBFunction getFunction(String name, int overload) {
        return overload > 0 ?
                functions.getObject(name, overload) :
                getObjectFallbackOnSynonym(functions, name);
    }

    public DBProgram getProgram(String name) {
        DBProgram program = getPackage(name);
        if (program == null) program = getType(name);
        return program;
    }

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
                ConnectionUtil.close(resultSet);
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
                ConnectionUtil.close(resultSet);
            }

        } finally {
            connectionHandler.freePoolConnection(connection);
        }

        for (BrowserTreeNode treeNode : refreshNodes) {
            EventUtil.notify(getProject(), BrowserTreeEventListener.TOPIC).nodeChanged(treeNode, TreeEventType.NODES_CHANGED);
        }

    }

    private Set<BrowserTreeNode> resetObjectsStatus() {
        ObjectStatusUpdater updater = new ObjectStatusUpdater();
        initChildObjects().visitLists(updater, true);
        return updater.getRefreshNodes();
    }

    class ObjectStatusUpdater extends DisposableBase implements DBObjectListVisitor {
        private Set<BrowserTreeNode> refreshNodes = new HashSet<BrowserTreeNode>();

        public void visitObjectList(DBObjectList<DBObject> objectList) {
            if (objectList.isLoaded() && !objectList.isDirty() && !objectList.isLoading()) {
                List<DBObject> objects = objectList.getObjects();
                for (DBObject object : objects) {
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

    public void buildToolTip(HtmlToolTipBuilder ttb) {
        ttb.append(true, getObjectType().getName(), true);
        ttb.createEmptyRow();
        super.buildToolTip(ttb);
    }

    /*********************************************************
     *                     TreeElement                       *
     *********************************************************/
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
        new DynamicContentResultSetLoader<DBTable>(SCHEMA, TABLE) {
            public ResultSet createResultSet(DynamicContent<DBTable> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadTables(schema.getName(), connection);
            }

            public DBTable createElement(DynamicContent<DBTable> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return new DBTableImpl(schema, resultSet);
            }
        };

        new DynamicContentResultSetLoader<DBView>(SCHEMA, VIEW){
            public ResultSet createResultSet(DynamicContent<DBView> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadViews(schema.getName(), connection);
            }

            public DBView createElement(DynamicContent<DBView> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return new DBViewImpl(schema, resultSet);
            }
        };

        new DynamicContentResultSetLoader<DBMaterializedView>(SCHEMA, MATERIALIZED_VIEW){
            public ResultSet createResultSet(DynamicContent<DBMaterializedView> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadMaterializedViews(schema.getName(), connection);
            }

            public DBMaterializedView createElement(DynamicContent<DBMaterializedView> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return new DBMaterializedViewImpl(schema, resultSet);
            }
        };

        new DynamicContentResultSetLoader<DBSynonym>(SCHEMA, SYNONYM) {
            public ResultSet createResultSet(DynamicContent<DBSynonym> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadSynonyms(schema.getName(), connection);
            }

            public DBSynonym createElement(DynamicContent<DBSynonym> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return new DBSynonymImpl(schema, resultSet);
            }
        };

        new DynamicContentResultSetLoader<DBSequence>(SCHEMA, SEQUENCE) {
            public ResultSet createResultSet(DynamicContent<DBSequence> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadSequences(schema.getName(), connection);
            }

            public DBSequence createElement(DynamicContent<DBSequence> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return new DBSequenceImpl(schema, resultSet);
            }
        };

        new DynamicContentResultSetLoader<DBProcedure>(SCHEMA, PROCEDURE) {
            public ResultSet createResultSet(DynamicContent<DBProcedure> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadProcedures(schema.getName(), connection);
            }

            public DBProcedure createElement(DynamicContent<DBProcedure> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return new DBProcedureImpl(schema, resultSet);
            }
        };

        new DynamicContentResultSetLoader<DBFunction>(SCHEMA, FUNCTION) {
            public ResultSet createResultSet(DynamicContent<DBFunction> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadFunctions(schema.getName(), connection);
            }
            public DBFunction createElement(DynamicContent<DBFunction> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return new DBFunctionImpl(schema, resultSet);
            }
        };

        new DynamicContentResultSetLoader<DBPackage>(SCHEMA, PACKAGE) {
            public ResultSet createResultSet(DynamicContent<DBPackage> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadPackages(schema.getName(), connection);
            }

            public DBPackage createElement(DynamicContent<DBPackage> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return new DBPackageImpl(schema, resultSet);
            }
        };

        new DynamicContentResultSetLoader<DBType>(SCHEMA, TYPE) {
            public ResultSet createResultSet(DynamicContent<DBType> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadTypes(schema.getName(), connection);
            }

            public DBType createElement(DynamicContent<DBType> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return new DBTypeImpl(schema, resultSet);
            }
        };

        new DynamicContentResultSetLoader<DBDatabaseTrigger>(SCHEMA, DATABASE_TRIGGER) {
            public ResultSet createResultSet(DynamicContent<DBDatabaseTrigger> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadDatabaseTriggers(schema.getName(), connection);
            }

            public DBDatabaseTrigger createElement(DynamicContent<DBDatabaseTrigger> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return new DBDatabaseTriggerImpl(schema, resultSet);
            }
        };

        new DynamicContentResultSetLoader<DBDimension>(SCHEMA, DIMENSION) {
            public ResultSet createResultSet(DynamicContent<DBDimension> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadDimensions(schema.getName(), connection);
            }

            public DBDimension createElement(DynamicContent<DBDimension> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return new DBDimensionImpl(schema, resultSet);
            }
        };

        new DynamicContentResultSetLoader<DBCluster>(SCHEMA, CLUSTER) {
            public ResultSet createResultSet(DynamicContent<DBCluster> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadClusters(schema.getName(), connection);
            }

            public DBCluster createElement(DynamicContent<DBCluster> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return new DBClusterImpl(schema, resultSet);
            }
        };

        new DynamicContentResultSetLoader<DBDatabaseLink>(SCHEMA, DBLINK) {
            public ResultSet createResultSet(DynamicContent dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadDatabaseLinks(schema.getName(), connection);
            }

            public DBDatabaseLink createElement(DynamicContent<DBDatabaseLink> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return new DBDatabaseLinkImpl(schema, resultSet);
            }
        };

        new DynamicContentResultSetLoader<DBColumn>(SCHEMA, COLUMN) {
            public ResultSet createResultSet(DynamicContent<DBColumn> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadAllColumns(schema.getName(), connection);
            }

            public DBColumn createElement(DynamicContent<DBColumn> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
                String datasetName = resultSet.getString("DATASET_NAME");

                DBDataset dataset = (DBDataset) loaderCache.getObject(datasetName);
                if (dataset == null) {
                    DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                    dataset = schema.getDataset(datasetName);
                    loaderCache.setObject(datasetName, dataset);
                }

                // dataset may be null if cluster column!!
                return dataset == null ? null : new DBColumnImpl(dataset, resultSet);
            }
        };

        new DynamicContentResultSetLoader<DBConstraint>(SCHEMA, CONSTRAINT) {
            public ResultSet createResultSet(DynamicContent<DBConstraint> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadAllConstraints(schema.getName(), connection);
            }

            public DBConstraint createElement(DynamicContent<DBConstraint> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
                String datasetName = resultSet.getString("DATASET_NAME");

                DBDataset dataset = (DBDataset) loaderCache.getObject(datasetName);
                if (dataset == null) {
                    DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                    dataset = schema.getDataset(datasetName);
                    loaderCache.setObject(datasetName, dataset);
                }

                return dataset == null ? null : new DBConstraintImpl(dataset, resultSet);
            }
        };

        new DynamicContentResultSetLoader<DBIndex>(SCHEMA, INDEX) {
            public ResultSet createResultSet(DynamicContent<DBIndex> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadAllIndexes(schema.getName(), connection);
            }

            public DBIndex createElement(DynamicContent<DBIndex> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
                String datasetName = resultSet.getString("TABLE_NAME");

                DBDataset dataset = (DBDataset) loaderCache.getObject(datasetName);
                if (dataset == null) {
                    DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                    dataset = schema.getDataset(datasetName);
                    loaderCache.setObject(datasetName, dataset);
                }

                return dataset == null ? null : new DBIndexImpl(dataset, resultSet);
            }
        };

        new DynamicContentResultSetLoader<DBDatasetTrigger>(SCHEMA, DATASET_TRIGGER) {
            public ResultSet createResultSet(DynamicContent<DBDatasetTrigger> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadAllDatasetTriggers(schema.getName(), connection);
            }

            public DBDatasetTrigger createElement(DynamicContent<DBDatasetTrigger> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
                String datasetName = resultSet.getString("DATASET_NAME");
                DBDataset dataset = (DBDataset) loaderCache.getObject(datasetName);
                if (dataset == null) {
                    DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                    dataset = schema.getDataset(datasetName);
                    loaderCache.setObject(datasetName, dataset);
                }
                return dataset == null ? null : new DBDatasetTriggerImpl(dataset, resultSet);
            }
        };

        new DynamicContentResultSetLoader<DBNestedTable>(SCHEMA, NESTED_TABLE) {
            public ResultSet createResultSet(DynamicContent dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadAllNestedTables(schema.getName(), connection);
            }

            public DBNestedTable createElement(DynamicContent<DBNestedTable> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
                String tableName = resultSet.getString("TABLE_NAME");
                DBTable table = (DBTable) loaderCache.getObject(tableName);
                if (table == null) {
                    DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                    table = schema.getTable(tableName);
                    loaderCache.setObject(tableName, table);
                }
                return table == null ? null : new DBNestedTableImpl(table, resultSet);
            }
        };

        new DynamicContentResultSetLoader<DBPackageFunction>(SCHEMA, PACKAGE_FUNCTION) {
            public ResultSet createResultSet(DynamicContent dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadAllPackageFunctions(schema.getName(), connection);
            }

            public DBPackageFunction createElement(DynamicContent<DBPackageFunction> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
                String packageName = resultSet.getString("PACKAGE_NAME");
                DBPackage packagee = (DBPackage) loaderCache.getObject(packageName);
                if (packagee == null) {
                    DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                    packagee = schema.getPackage(packageName);
                    loaderCache.setObject(packageName, packagee);
                }
                return packagee == null ? null : new DBPackageFunctionImpl(packagee, resultSet);
            }
        };

        new DynamicContentResultSetLoader<DBPackageProcedure>(SCHEMA, PACKAGE_PROCEDURE) {
            public ResultSet createResultSet(DynamicContent dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadAllPackageProcedures(schema.getName(), connection);
            }

            public DBPackageProcedure createElement(DynamicContent<DBPackageProcedure> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
                String packageName = resultSet.getString("PACKAGE_NAME");
                DBPackage packagee = (DBPackage) loaderCache.getObject(packageName);
                if (packagee == null) {
                    DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                    packagee = schema.getPackage(packageName);
                    loaderCache.setObject(packageName, packagee);
                }
                return packagee == null ? null : new DBPackageProcedureImpl(packagee, resultSet);
            }
        };

        new DynamicContentResultSetLoader<DBPackageType>(SCHEMA, PACKAGE_TYPE) {
            public ResultSet createResultSet(DynamicContent<DBPackageType> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadAllPackageTypes(schema.getName(), connection);
            }

            public DBPackageType createElement(DynamicContent<DBPackageType> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
                String packageName = resultSet.getString("PACKAGE_NAME");
                DBPackage packagee = (DBPackage) loaderCache.getObject(packageName);
                if (packagee == null) {
                    DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                    packagee = schema.getPackage(packageName);
                    loaderCache.setObject(packageName, packagee);
                }
                return packagee == null ? null : new DBPackageTypeImpl(packagee, resultSet);
            }
        };

        new DynamicContentResultSetLoader<DBTypeAttribute>(SCHEMA, TYPE_ATTRIBUTE) {
            public ResultSet createResultSet(DynamicContent<DBTypeAttribute> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadAllTypeAttributes(schema.getName(), connection);
            }

            public DBTypeAttribute createElement(DynamicContent<DBTypeAttribute> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
                String typeName = resultSet.getString("TYPE_NAME");
                DBType type = (DBType) loaderCache.getObject(typeName);
                if (type == null) {
                    DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                    type = schema.getType(typeName);
                    loaderCache.setObject(typeName, type);
                }
                return type == null ? null : new DBTypeAttributeImpl(type, resultSet);
            }
        };

        new DynamicContentResultSetLoader<DBTypeFunction>(SCHEMA, TYPE_FUNCTION) {
            public ResultSet createResultSet(DynamicContent dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadAllTypeFunctions(schema.getName(), connection);
            }

            public DBTypeFunction createElement(DynamicContent<DBTypeFunction> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
                String typeName = resultSet.getString("TYPE_NAME");
                DBType type = (DBType) loaderCache.getObject(typeName);
                if (type == null) {
                    DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                    type = schema.getType(typeName);
                    loaderCache.setObject(typeName, type);
                }
                return type == null ?  null : new DBTypeFunctionImpl(type, resultSet);
            }
        };

        new DynamicContentResultSetLoader<DBTypeProcedure>(SCHEMA, TYPE_PROCEDURE) {
            public ResultSet createResultSet(DynamicContent dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadAllTypeProcedures(schema.getName(), connection);
            }

            public DBTypeProcedure createElement(DynamicContent<DBTypeProcedure> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
                String typeName = resultSet.getString("TYPE_NAME");
                DBType type = (DBType) loaderCache.getObject(typeName);
                if (type == null) {
                    DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                    type = schema.getType(typeName);
                    loaderCache.setObject(typeName, type);
                }
                return type == null ? null : new DBTypeProcedureImpl(type, resultSet);
            }
        };

        new DynamicContentResultSetLoader<DBArgument>(SCHEMA, ARGUMENT) {
            public ResultSet createResultSet(DynamicContent<DBArgument> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadAllMethodArguments(schema.getName(), connection);
            }

            public DBArgument createElement(DynamicContent<DBArgument> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
                String programName = resultSet.getString("PROGRAM_NAME");
                String methodName = resultSet.getString("METHOD_NAME");
                String methodType = resultSet.getString("METHOD_TYPE");
                int overload = resultSet.getInt("OVERLOAD");
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                DBProgram program = programName == null ? null : schema.getProgram(programName);

                String cacheKey = methodName + methodType + overload;
                DBMethod method = (DBMethod) loaderCache.getObject(cacheKey);
                DBObjectType objectType = get(methodType);

                if (method == null || method.getProgram() != program || method.getOverload() != overload) {
                    if (programName == null) {
                        method = schema.getMethod(methodName, objectType, overload);
                    } else {
                        method = program == null ? null : program.getMethod(methodName, overload);
                    }
                    loaderCache.setObject(cacheKey, method);
                }
                return method == null ? null : new DBArgumentImpl(method, resultSet);
            }
        };

        new DynamicContentResultSetLoader(SCHEMA, CONSTRAINT_COLUMN) {
            public ResultSet createResultSet(DynamicContent dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadAllConstraintRelations(schema.getName(), connection);
            }

            public DynamicContentElement createElement(DynamicContent dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
                String datasetName = resultSet.getString("DATASET_NAME");
                String columnName = resultSet.getString("COLUMN_NAME");
                String constraintName = resultSet.getString("CONSTRAINT_NAME");
                int position = resultSet.getInt("POSITION");

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
                DBDataset dataset = (DBDataset) loaderCache.getObject(datasetName);
                if (dataset == null) {
                    DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                    dataset = schema.getDataset(datasetName);
                    loaderCache.setObject(datasetName, dataset);
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

        new DynamicContentResultSetLoader(SCHEMA, INDEX_COLUMN) {
            public ResultSet createResultSet(DynamicContent dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
                DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                return metadataInterface.loadAllIndexRelations(schema.getName(), connection);
            }

            public DBObjectRelation createElement(DynamicContent dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
                String datasetName = resultSet.getString("TABLE_NAME");
                String columnName = resultSet.getString("COLUMN_NAME");
                String indexName = resultSet.getString("INDEX_NAME");

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

                DBDataset dataset = (DBDataset) loaderCache.getObject(datasetName);
                if (dataset == null) {
                    DBSchema schema = (DBSchema) dynamicContent.getParentElement();
                    dataset = schema.getDataset(datasetName);
                    loaderCache.setObject(datasetName, dataset);
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
