package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.browser.model.BrowserTreeEventListener;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.loader.DynamicContentResultSetLoader;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.thread.Background;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.util.Lists;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.Resources;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.common.metadata.def.*;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterfaceInvoker;
import com.dci.intellij.dbn.database.interfaces.DatabaseMetadataInterface;
import com.dci.intellij.dbn.database.interfaces.queue.InterfaceTaskDefinition;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.*;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectImpl;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectListVisitor;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.object.common.status.DBObjectStatusHolder;
import com.dci.intellij.dbn.object.type.DBObjectType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.dci.intellij.dbn.common.Priority.LOW;
import static com.dci.intellij.dbn.common.content.DynamicContentProperty.HIDDEN;
import static com.dci.intellij.dbn.common.content.DynamicContentProperty.*;
import static com.dci.intellij.dbn.common.dispose.Failsafe.nd;
import static com.dci.intellij.dbn.common.util.Commons.nvl;
import static com.dci.intellij.dbn.common.util.Unsafe.cast;
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

    private Latent<List<DBColumn>> primaryKeyColumns;
    private Latent<List<DBColumn>> foreignKeyColumns;

    public DBSchemaImpl(ConnectionHandler connection, DBSchemaMetadata metadata) throws SQLException {
        super(connection, metadata);
    }

    @Override
    protected String initObject(DBSchemaMetadata metadata) throws SQLException {
        String name = metadata.getSchemaName();
        set(PUBLIC_SCHEMA, metadata.isPublic());
        set(SYSTEM_SCHEMA, metadata.isSystem());
        set(EMPTY_SCHEMA, metadata.isEmpty());
        set(USER_SCHEMA, Strings.equalsIgnoreCase(name, this.getConnection().getUserName()));
        return name;
    }

    @Override
    protected void initLists() {
        DBObjectListContainer childObjects = ensureChildObjects();

        tables             = childObjects.createObjectList(TABLE,             this);
        views              = childObjects.createObjectList(VIEW,              this);
        materializedViews  = childObjects.createObjectList(MATERIALIZED_VIEW, this);
        synonyms           = childObjects.createObjectList(SYNONYM,           this);
        sequences          = childObjects.createObjectList(SEQUENCE,          this);
        procedures         = childObjects.createObjectList(PROCEDURE,         this);
        functions          = childObjects.createObjectList(FUNCTION,          this);
        packages           = childObjects.createObjectList(PACKAGE,           this);
        types              = childObjects.createObjectList(TYPE,              this);
        databaseTriggers   = childObjects.createObjectList(DATABASE_TRIGGER,  this);
        dimensions         = childObjects.createObjectList(DIMENSION,         this);
        clusters           = childObjects.createObjectList(CLUSTER,           this);
        databaseLinks      = childObjects.createObjectList(DBLINK,            this);

        DBObjectList<DBConstraint> constraints = childObjects.createObjectList(CONSTRAINT, this, INTERNAL, GROUPED);
        DBObjectList<DBIndex> indexes          = childObjects.createObjectList(INDEX,      this, INTERNAL, GROUPED);
        DBObjectList<DBColumn> columns         = childObjects.createObjectList(COLUMN,     this, INTERNAL, GROUPED, HIDDEN);

        childObjects.createObjectList(DATASET_TRIGGER,   this, INTERNAL, GROUPED);
        childObjects.createObjectList(NESTED_TABLE,      this, INTERNAL, GROUPED, HIDDEN);
        childObjects.createObjectList(PACKAGE_FUNCTION,  this, INTERNAL, GROUPED, HIDDEN);
        childObjects.createObjectList(PACKAGE_PROCEDURE, this, INTERNAL, GROUPED, HIDDEN);
        childObjects.createObjectList(PACKAGE_TYPE,      this, INTERNAL, GROUPED, HIDDEN);
        childObjects.createObjectList(TYPE_ATTRIBUTE,    this, INTERNAL, GROUPED, HIDDEN);
        childObjects.createObjectList(TYPE_FUNCTION,     this, INTERNAL, GROUPED, HIDDEN);
        childObjects.createObjectList(TYPE_PROCEDURE,    this, INTERNAL, GROUPED, HIDDEN);
        childObjects.createObjectList(ARGUMENT,          this, INTERNAL, GROUPED, HIDDEN);

        //ol.createHiddenObjectList(DBObjectType.TYPE_METHOD, this, TYPE_METHODS_LOADER);

        childObjects.createObjectRelationList(CONSTRAINT_COLUMN, this, constraints, columns, INTERNAL, GROUPED);
        childObjects.createObjectRelationList(INDEX_COLUMN, this, indexes, columns, INTERNAL, GROUPED);

        this.primaryKeyColumns = Latent.mutable(
                () -> nd(columns).getSignature(),
                () -> nvl(Lists.filter(nd(columns).getObjects(), c -> c.isPrimaryKey()), Collections.emptyList()));

        this.foreignKeyColumns = Latent.mutable(
                () -> nd(columns).getSignature(),
                () -> nvl(Lists.filter(nd(columns).getObjects(), c -> c.isForeignKey()), Collections.emptyList()));
    }

    @Override
    public void initProperties() {
        properties.set(ROOT_OBJECT, true);
    }

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
    public DBObject getChildObject(DBObjectType objectType, String name, short overload, boolean lookupHidden) {
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
    protected @Nullable List<DBObjectNavigationList> createNavigationLists() {
        DBUser user = getOwner();
        if (user != null) {
            LinkedList<DBObjectNavigationList> navigationLists = new LinkedList<>();
            navigationLists.add(DBObjectNavigationList.create("User", user));
            return navigationLists;
        }
        return null;
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
        return ensureChildObjects().getObjects(INDEX, true);
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

    public List<DBColumn> getPrimaryKeyColumns() {
        return primaryKeyColumns.get();
    }

    public List<DBColumn> getForeignKeyColumns() {
        return foreignKeyColumns.get();
    }

    @Override
    public List<DBDatasetTrigger> getDatasetTriggers() {
        return ensureChildObjects().getObjects(DATASET_TRIGGER, true);
    }

    @Override
    public List<DBDatabaseTrigger> getDatabaseTriggers() {
        return ensureChildObjects().getObjects(DATABASE_TRIGGER, false);
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
        DBObjectList indexList = getChildObjectList(INDEX);
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
        List<DBDataset> datasets = new ArrayList<>();
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
            if (dataset == null && MATERIALIZED_VIEW.isSupported(this)) {
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
        if (object == null && SYNONYM.isSupported(this)) {
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
    public DBProcedure getProcedure(String name, short overload) {
        return overload > 0 ?
                procedures.getObject(name, overload) :
                getObjectFallbackOnSynonym(procedures, name);
    }

    @Override
    public DBFunction getFunction(String name, short overload) {
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
    public DBMethod getMethod(String name, DBObjectType methodType, short overload) {
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
    public DBMethod getMethod(String name, short overload) {
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
    public void refreshObjectsStatus() throws SQLException {
        Set<BrowserTreeNode> refreshNodes = resetObjectsStatus();
        ConnectionHandler connection = this.getConnection();

        InterfaceTaskDefinition taskDefinition = InterfaceTaskDefinition.create(LOW,
                "Refreshing object status",
                "Refreshing object status for " + getQualifiedNameWithType(),
                connection.getInterfaceContext());

        DatabaseInterfaceInvoker.schedule(taskDefinition, conn -> {
            refreshValidStatus(refreshNodes, conn);
            refreshDebugStatus(refreshNodes, conn);
            Background.run(() ->
                    refreshNodes.forEach(n -> ProjectEvents.notify(getProject(), BrowserTreeEventListener.TOPIC,
                            l -> l.nodeChanged(n, TreeEventType.NODES_CHANGED))));
        });


    }

    private void refreshValidStatus(Set<BrowserTreeNode> refreshNodes, DBNConnection c) throws SQLException {
        ResultSet resultSet = null;
        try {
            DatabaseMetadataInterface metadata = getMetadataInterface();
            resultSet = metadata.loadInvalidObjects(getName(), c);
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
                    } else {
                        statusChanged = objectStatus.set(DBObjectStatus.VALID, false);
                    }
                    if (statusChanged) {
                        refreshNodes.add(schemaObject.getParent());
                    }
                }
            }
        } finally {
            Resources.close(resultSet);
        }
    }

    private void refreshDebugStatus(Set<BrowserTreeNode> refreshNodes, DBNConnection c) throws SQLException {
        ResultSet resultSet = null;
        try {
            DatabaseMetadataInterface metadata = getMetadataInterface();
            resultSet = metadata.loadDebugObjects(getName(), c);
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
                    } else {
                        statusChanged = objectStatus.set(DBObjectStatus.DEBUG, true);
                    }
                    if (statusChanged) {
                        refreshNodes.add(schemaObject.getParent());
                    }
                }
            }
        } finally {
            Resources.close(resultSet);
        }
    }

    @Override
    public SchemaId getIdentifier() {
        return SchemaId.get(getName());
    }

    private Set<BrowserTreeNode> resetObjectsStatus() {
        ObjectStatusUpdater updater = new ObjectStatusUpdater();
        ensureChildObjects().visitObjects(updater, true);
        return updater.getRefreshNodes();
    }

    @Getter
    static class ObjectStatusUpdater extends Base implements DBObjectListVisitor {
        private final Set<BrowserTreeNode> refreshNodes = new HashSet<>();

        @Override
        public void visit(DBObjectList<?> objectList) {
            if (objectList.isLoaded() && !objectList.isDirty() && !objectList.isLoading()) {
                List<DBObject> objects = cast(objectList.getObjects());
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

        @Override
        protected void disposeInner() {
            nullify();
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
            public ResultSet createResultSet(DynamicContent<DBTable> content, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadata = content.getMetadataInterface();
                DBSchema schema = content.ensureParentEntity();
                return metadata.loadTables(schema.getName(), connection);
            }

            @Override
            public DBTable createElement(DynamicContent<DBTable> content, DBTableMetadata metadata, LoaderCache cache) throws SQLException {
                DBSchema schema = content.getParentEntity();
                return new DBTableImpl(schema, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBView, DBViewMetadata>(SCHEMA, VIEW, true, true){
            @Override
            public ResultSet createResultSet(DynamicContent<DBView> content, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadata = content.getMetadataInterface();
                DBSchema schema = content.ensureParentEntity();
                return metadata.loadViews(schema.getName(), connection);
            }

            @Override
            public DBView createElement(DynamicContent<DBView> content, DBViewMetadata metadata, LoaderCache cache) throws SQLException {
                DBSchema schema = content.getParentEntity();
                return new DBViewImpl(schema, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBMaterializedView, DBMaterializedViewMetadata>(SCHEMA, MATERIALIZED_VIEW, true, true){
            @Override
            public ResultSet createResultSet(DynamicContent<DBMaterializedView> content, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadata = content.getMetadataInterface();
                DBSchema schema = content.ensureParentEntity();
                return metadata.loadMaterializedViews(schema.getName(), connection);
            }

            @Override
            public DBMaterializedView createElement(DynamicContent<DBMaterializedView> content, DBMaterializedViewMetadata metadata, LoaderCache cache) throws SQLException {
                DBSchema schema = content.getParentEntity();
                return new DBMaterializedViewImpl(schema, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBSynonym, DBSynonymMetadata>(SCHEMA, SYNONYM, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBSynonym> content, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadata = content.getMetadataInterface();
                DBSchema schema = content.ensureParentEntity();
                return metadata.loadSynonyms(schema.getName(), connection);
            }

            @Override
            public DBSynonym createElement(DynamicContent<DBSynonym> content, DBSynonymMetadata metadata, LoaderCache cache) throws SQLException {
                DBSchema schema = content.getParentEntity();
                return new DBSynonymImpl(schema, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBSequence, DBSequenceMetadata>(SCHEMA, SEQUENCE, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBSequence> content, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadata = content.getMetadataInterface();
                DBSchema schema = content.ensureParentEntity();
                return metadata.loadSequences(schema.getName(), connection);
            }

            @Override
            public DBSequence createElement(DynamicContent<DBSequence> content, DBSequenceMetadata metadata, LoaderCache cache) throws SQLException {
                DBSchema schema = content.getParentEntity();
                return new DBSequenceImpl(schema, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBProcedure, DBProcedureMetadata>(SCHEMA, PROCEDURE, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBProcedure> content, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadata = content.getMetadataInterface();
                DBSchema schema = content.ensureParentEntity();
                return metadata.loadProcedures(schema.getName(), connection);
            }

            @Override
            public DBProcedure createElement(DynamicContent<DBProcedure> content, DBProcedureMetadata metadata, LoaderCache cache) throws SQLException {
                DBSchema schema = content.getParentEntity();
                return new DBProcedureImpl(schema, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBFunction, DBFunctionMetadata>(SCHEMA, FUNCTION, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBFunction> content, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadata = content.getMetadataInterface();
                DBSchema schema = content.ensureParentEntity();
                return metadata.loadFunctions(schema.getName(), connection);
            }
            @Override
            public DBFunction createElement(DynamicContent<DBFunction> content, DBFunctionMetadata metadata, LoaderCache cache) throws SQLException {
                DBSchema schema = content.getParentEntity();
                return new DBFunctionImpl(schema, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBPackage, DBPackageMetadata>(SCHEMA, PACKAGE, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBPackage> content, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadata = content.getMetadataInterface();
                DBSchema schema = content.ensureParentEntity();
                return metadata.loadPackages(schema.getName(), connection);
            }

            @Override
            public DBPackage createElement(DynamicContent<DBPackage> content, DBPackageMetadata metadata, LoaderCache cache) throws SQLException {
                DBSchema schema = content.getParentEntity();
                return new DBPackageImpl(schema, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBType, DBTypeMetadata>(SCHEMA, TYPE, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBType> content, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadata = content.getMetadataInterface();
                DBSchema schema = content.ensureParentEntity();
                return metadata.loadTypes(schema.getName(), connection);
            }

            @Override
            public DBType createElement(DynamicContent<DBType> content, DBTypeMetadata metadata, LoaderCache cache) throws SQLException {
                DBSchema schema = content.getParentEntity();
                return new DBTypeImpl(schema, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBDatabaseTrigger, DBTriggerMetadata>(SCHEMA, DATABASE_TRIGGER, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBDatabaseTrigger> content, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadata = content.getMetadataInterface();
                DBSchema schema = content.ensureParentEntity();
                return metadata.loadDatabaseTriggers(schema.getName(), connection);
            }

            @Override
            public DBDatabaseTrigger createElement(DynamicContent<DBDatabaseTrigger> content, DBTriggerMetadata metadata, LoaderCache cache) throws SQLException {
                DBSchema schema = content.getParentEntity();
                return new DBDatabaseTriggerImpl(schema, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBDimension, DBDimensionMetadata>(SCHEMA, DIMENSION, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBDimension> content, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadata = content.getMetadataInterface();
                DBSchema schema = content.ensureParentEntity();
                return metadata.loadDimensions(schema.getName(), connection);
            }

            @Override
            public DBDimension createElement(DynamicContent<DBDimension> content, DBDimensionMetadata metadata, LoaderCache cache) throws SQLException {
                DBSchema schema = content.getParentEntity();
                return new DBDimensionImpl(schema, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBCluster, DBClusterMetadata>(SCHEMA, CLUSTER, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBCluster> content, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadata = content.getMetadataInterface();
                DBSchema schema = content.ensureParentEntity();
                return metadata.loadClusters(schema.getName(), connection);
            }

            @Override
            public DBCluster createElement(DynamicContent<DBCluster> content, DBClusterMetadata metadata, LoaderCache cache) throws SQLException {
                DBSchema schema = content.getParentEntity();
                return new DBClusterImpl(schema, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBDatabaseLink, DBDatabaseLinkMetadata>(SCHEMA, DBLINK, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBDatabaseLink> content, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadata = content.getMetadataInterface();
                DBSchema schema = content.ensureParentEntity();
                return metadata.loadDatabaseLinks(schema.getName(), connection);
            }

            @Override
            public DBDatabaseLink createElement(DynamicContent<DBDatabaseLink> content, DBDatabaseLinkMetadata metadata, LoaderCache cache) throws SQLException {
                DBSchema schema = content.getParentEntity();
                return new DBDatabaseLinkImpl(schema, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBColumn, DBColumnMetadata>(SCHEMA, COLUMN, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBColumn> content, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadata = content.getMetadataInterface();
                DBSchema schema = content.ensureParentEntity();
                return metadata.loadAllColumns(schema.getName(), connection);
            }

            @Override
            public DBColumn createElement(DynamicContent<DBColumn> content, DBColumnMetadata provider, LoaderCache cache) throws SQLException {
                String datasetName = provider.getDatasetName();

                DBDataset dataset = (DBDataset) cache.getObject(datasetName);
                if (dataset == null) {
                    DBSchema schema = content.ensureParentEntity();
                    dataset = schema.getDataset(datasetName);
                    cache.setObject(datasetName, dataset);
                }

                // dataset may be null if cluster column!!
                return dataset == null ? null : new DBColumnImpl(dataset, provider);
            }
        };

        new DynamicContentResultSetLoader<DBConstraint, DBConstraintMetadata>(SCHEMA, CONSTRAINT, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBConstraint> content, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadata = content.getMetadataInterface();
                DBSchema schema = content.ensureParentEntity();
                return metadata.loadAllConstraints(schema.getName(), connection);
            }

            @Override
            public DBConstraint createElement(DynamicContent<DBConstraint> content, DBConstraintMetadata metadata, LoaderCache cache) throws SQLException {
                String datasetName = metadata.getDatasetName();

                DBDataset dataset = (DBDataset) cache.getObject(datasetName);
                if (dataset == null) {
                    DBSchema schema = content.ensureParentEntity();
                    dataset = schema.getDataset(datasetName);
                    cache.setObject(datasetName, dataset);
                }

                return dataset == null ? null : new DBConstraintImpl(dataset, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBIndex, DBIndexMetadata>(SCHEMA, INDEX, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBIndex> content, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadata = content.getMetadataInterface();
                DBSchema schema = content.ensureParentEntity();
                return metadata.loadAllIndexes(schema.getName(), connection);
            }

            @Override
            public DBIndex createElement(DynamicContent<DBIndex> content, DBIndexMetadata metadata, LoaderCache cache) throws SQLException {
                String datasetName = metadata.getTableName();

                DBDataset dataset = (DBDataset) cache.getObject(datasetName);
                if (dataset == null) {
                    DBSchema schema = content.ensureParentEntity();
                    dataset = schema.getDataset(datasetName);
                    cache.setObject(datasetName, dataset);
                }

                return dataset == null ? null : new DBIndexImpl(dataset, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBDatasetTrigger, DBTriggerMetadata>(SCHEMA, DATASET_TRIGGER, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBDatasetTrigger> content, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadata = content.getMetadataInterface();
                DBSchema schema = content.ensureParentEntity();
                return metadata.loadAllDatasetTriggers(schema.getName(), connection);
            }

            @Override
            public DBDatasetTrigger createElement(DynamicContent<DBDatasetTrigger> content, DBTriggerMetadata metadata, LoaderCache cache) throws SQLException {
                String datasetName = metadata.getDatasetName();
                DBDataset dataset = (DBDataset) cache.getObject(datasetName);
                if (dataset == null) {
                    DBSchema schema = content.ensureParentEntity();
                    dataset = schema.getDataset(datasetName);
                    cache.setObject(datasetName, dataset);
                }
                return dataset == null ? null : new DBDatasetTriggerImpl(dataset, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBNestedTable, DBNestedTableMetadata>(SCHEMA, NESTED_TABLE, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBNestedTable> content, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadata = content.getMetadataInterface();
                DBSchema schema = content.ensureParentEntity();
                return metadata.loadAllNestedTables(schema.getName(), connection);
            }

            @Override
            public DBNestedTable createElement(DynamicContent<DBNestedTable> content, DBNestedTableMetadata metadata, LoaderCache cache) throws SQLException {
                String tableName = metadata.getTableName();
                DBTable table = (DBTable) cache.getObject(tableName);
                if (table == null) {
                    DBSchema schema = content.ensureParentEntity();
                    table = schema.getTable(tableName);
                    cache.setObject(tableName, table);
                }
                return table == null ? null : new DBNestedTableImpl(table, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBPackageFunction, DBFunctionMetadata>(SCHEMA, PACKAGE_FUNCTION, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBPackageFunction> content, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadata = content.getMetadataInterface();
                DBSchema schema = content.ensureParentEntity();
                return metadata.loadAllPackageFunctions(schema.getName(), connection);
            }

            @Override
            public DBPackageFunction createElement(DynamicContent<DBPackageFunction> content, DBFunctionMetadata metadata, LoaderCache cache) throws SQLException {
                String packageName = metadata.getPackageName();
                DBPackage packagee = (DBPackage) cache.getObject(packageName);
                if (packagee == null) {
                    DBSchema schema = content.ensureParentEntity();
                    packagee = schema.getPackage(packageName);
                    cache.setObject(packageName, packagee);
                }
                return packagee == null ? null : new DBPackageFunctionImpl(packagee, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBPackageProcedure, DBProcedureMetadata>(SCHEMA, PACKAGE_PROCEDURE, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBPackageProcedure> content, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadata = content.getMetadataInterface();
                DBSchema schema = content.ensureParentEntity();
                return metadata.loadAllPackageProcedures(schema.getName(), connection);
            }

            @Override
            public DBPackageProcedure createElement(DynamicContent<DBPackageProcedure> content, DBProcedureMetadata metadata, LoaderCache cache) throws SQLException {
                String packageName = metadata.getPackageName();
                DBPackage packagee = (DBPackage) cache.getObject(packageName);
                if (packagee == null) {
                    DBSchema schema = content.ensureParentEntity();
                    packagee = schema.getPackage(packageName);
                    cache.setObject(packageName, packagee);
                }
                return packagee == null ? null : new DBPackageProcedureImpl(packagee, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBPackageType, DBTypeMetadata>(SCHEMA, PACKAGE_TYPE, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBPackageType> content, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadata = content.getMetadataInterface();
                DBSchema schema = content.ensureParentEntity();
                return metadata.loadAllPackageTypes(schema.getName(), connection);
            }

            @Override
            public DBPackageType createElement(DynamicContent<DBPackageType> content, DBTypeMetadata metadata, LoaderCache cache) throws SQLException {
                String packageName = metadata.getPackageName();
                DBPackage packagee = (DBPackage) cache.getObject(packageName);
                if (packagee == null) {
                    DBSchema schema = content.ensureParentEntity();
                    packagee = schema.getPackage(packageName);
                    cache.setObject(packageName, packagee);
                }
                return packagee == null ? null : new DBPackageTypeImpl(packagee, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBTypeAttribute, DBTypeAttributeMetadata>(SCHEMA, TYPE_ATTRIBUTE, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBTypeAttribute> content, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadata = content.getMetadataInterface();
                DBSchema schema = content.ensureParentEntity();
                return metadata.loadAllTypeAttributes(schema.getName(), connection);
            }

            @Override
            public DBTypeAttribute createElement(DynamicContent<DBTypeAttribute> content, DBTypeAttributeMetadata provider, LoaderCache cache) throws SQLException {
                String typeName = provider.getTypeName();
                DBType type = (DBType) cache.getObject(typeName);
                if (type == null) {
                    DBSchema schema = content.ensureParentEntity();
                    type = schema.getType(typeName);
                    cache.setObject(typeName, type);
                }
                return type == null ? null : new DBTypeAttributeImpl(type, provider);
            }
        };

        new DynamicContentResultSetLoader<DBTypeFunction, DBFunctionMetadata>(SCHEMA, TYPE_FUNCTION, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBTypeFunction> content, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadata = content.getMetadataInterface();
                DBSchema schema = content.ensureParentEntity();
                return metadata.loadAllTypeFunctions(schema.getName(), connection);
            }

            @Override
            public DBTypeFunction createElement(DynamicContent<DBTypeFunction> content, DBFunctionMetadata metadata, LoaderCache cache) throws SQLException {
                String typeName = metadata.getTypeName();
                DBType type = (DBType) cache.getObject(typeName);
                if (type == null) {
                    DBSchema schema = content.ensureParentEntity();
                    type = schema.getType(typeName);
                    cache.setObject(typeName, type);
                }
                return type == null ?  null : new DBTypeFunctionImpl(type, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBTypeProcedure, DBProcedureMetadata>(SCHEMA, TYPE_PROCEDURE, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBTypeProcedure> content, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadata = content.getMetadataInterface();
                DBSchema schema = content.ensureParentEntity();
                return metadata.loadAllTypeProcedures(schema.getName(), connection);
            }

            @Override
            public DBTypeProcedure createElement(DynamicContent<DBTypeProcedure> content, DBProcedureMetadata metadata, LoaderCache cache) throws SQLException {
                String typeName = metadata.getTypeName();
                DBType type = (DBType) cache.getObject(typeName);
                if (type == null) {
                    DBSchema schema = content.ensureParentEntity();
                    type = schema.getType(typeName);
                    cache.setObject(typeName, type);
                }
                return type == null ? null : new DBTypeProcedureImpl(type, metadata);
            }
        };

        new DynamicContentResultSetLoader<DBArgument, DBArgumentMetadata>(SCHEMA, ARGUMENT, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBArgument> content, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadata = content.getMetadataInterface();
                DBSchema schema = content.ensureParentEntity();
                return metadata.loadAllMethodArguments(schema.getName(), connection);
            }

            @Override
            public DBArgument createElement(DynamicContent<DBArgument> content, DBArgumentMetadata metadata, LoaderCache cache) throws SQLException {
                String programName = metadata.getProgramName();
                String methodName = metadata.getMethodName();
                String methodType = metadata.getMethodType();
                short overload = metadata.getOverload();
                DBSchema schema = content.ensureParentEntity();
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
            public ResultSet createResultSet(DynamicContent<DBConstraintColumnRelation> content, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadata = content.getMetadataInterface();
                DBSchema schema = content.ensureParentEntity();
                return metadata.loadAllConstraintRelations(schema.getName(), connection);
            }

            @Override
            public DBConstraintColumnRelation createElement(DynamicContent<DBConstraintColumnRelation> content, DBConstraintColumnMetadata metadata, LoaderCache cache) throws SQLException {
                String datasetName = metadata.getDatasetName();
                String columnName = metadata.getColumnName();
                String constraintName = metadata.getConstraintName();
                short position = metadata.getPosition();

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
                    DBSchema schema = content.ensureParentEntity();
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
            public ResultSet createResultSet(DynamicContent<DBIndexColumnRelation> content, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadata = content.getMetadataInterface();
                DBSchema schema = content.ensureParentEntity();
                return metadata.loadAllIndexRelations(schema.getName(), connection);
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
                    DBSchema schema = content.ensureParentEntity();
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
