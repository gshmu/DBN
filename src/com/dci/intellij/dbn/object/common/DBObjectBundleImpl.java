package com.dci.intellij.dbn.object.common;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.browser.model.BrowserTreeEventListener;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.model.BrowserTreeNodeBase;
import com.dci.intellij.dbn.browser.model.LoadInProgressTreeNode;
import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentElement;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.content.loader.DynamicContentResultSetLoader;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.lookup.ConsumerStoppedException;
import com.dci.intellij.dbn.common.lookup.LookupConsumer;
import com.dci.intellij.dbn.common.notification.NotificationUtil;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleBackgroundInvocator;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionPool;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.data.type.DBNativeDataType;
import com.dci.intellij.dbn.data.type.DataTypeDefinition;
import com.dci.intellij.dbn.database.*;
import com.dci.intellij.dbn.editor.code.SourceCodeEditor;
import com.dci.intellij.dbn.editor.code.SourceCodeManagerAdapter;
import com.dci.intellij.dbn.editor.code.SourceCodeManagerListener;
import com.dci.intellij.dbn.execution.compiler.CompileManagerListener;
import com.dci.intellij.dbn.execution.statement.DataDefinitionChangeListener;
import com.dci.intellij.dbn.object.*;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectListImpl;
import com.dci.intellij.dbn.object.common.list.DBObjectRelationListContainer;
import com.dci.intellij.dbn.object.impl.*;
import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.dci.intellij.dbn.common.content.DynamicContentStatus.INDEXED;

public class DBObjectBundleImpl extends BrowserTreeNodeBase implements DBObjectBundle {
    private ConnectionHandler connectionHandler;
    private BrowserTreeNode treeParent;
    private List<BrowserTreeNode> allPossibleTreeChildren;
    private List<BrowserTreeNode> visibleTreeChildren;
    private boolean treeChildrenLoaded;

    private DBObjectList<DBSchema> schemas;
    private DBObjectList<DBUser> users;
    private DBObjectList<DBRole> roles;
    private DBObjectList<DBSystemPrivilege> systemPrivileges;
    private DBObjectList<DBObjectPrivilege> objectPrivileges;
    private DBObjectList<DBCharset> charsets;

    private List<DBNativeDataType> nativeDataTypes;
    private List<DBDataType> cachedDataTypes = new CopyOnWriteArrayList<>();

    private DBObjectListContainer objectLists;
    private DBObjectRelationListContainer objectRelationLists;
    private int connectionConfigHash;

    public DBObjectBundleImpl(ConnectionHandler connectionHandler, BrowserTreeNode treeParent) {
        this.connectionHandler = connectionHandler;
        this.treeParent = treeParent;
        connectionConfigHash = connectionHandler.getSettings().getDatabaseSettings().hashCode();

        this.objectLists = new DBObjectListContainer(this);
        users = objectLists.createObjectList(DBObjectType.USER, this, USERS_LOADER, INDEXED);
        schemas = objectLists.createObjectList(DBObjectType.SCHEMA, this, SCHEMAS_LOADER, new DBObjectList[]{users}, INDEXED);
        roles = objectLists.createObjectList(DBObjectType.ROLE, this, ROLES_LOADER, INDEXED);
        systemPrivileges = objectLists.createObjectList(DBObjectType.SYSTEM_PRIVILEGE, this, SYSTEM_PRIVILEGES_LOADER, INDEXED);
        charsets = objectLists.createObjectList(DBObjectType.CHARSET, this, CHARSETS_LOADER, INDEXED);
        allPossibleTreeChildren = DatabaseBrowserUtils.createList(schemas, users, roles, systemPrivileges, charsets);

        objectRelationLists = new DBObjectRelationListContainer(this);
        objectRelationLists.createObjectRelationList(
                DBObjectRelationType.USER_ROLE, this,
                "User role relations",
                USER_ROLE_RELATION_LOADER,
                users, roles);

        objectRelationLists.createObjectRelationList(
                DBObjectRelationType.USER_PRIVILEGE, this,
                "User privilege relations",
                USER_PRIVILEGE_RELATION_LOADER,
                users, systemPrivileges);

        objectRelationLists.createObjectRelationList(
                DBObjectRelationType.ROLE_ROLE, this,
                "Role role relations",
                ROLE_ROLE_RELATION_LOADER,
                roles);

        objectRelationLists.createObjectRelationList(
                DBObjectRelationType.ROLE_PRIVILEGE, this,
                "Role privilege relations",
                ROLE_PRIVILEGE_RELATION_LOADER,
                roles, systemPrivileges);

        objectLists.compact();
        objectRelationLists.compact();
        Project project = connectionHandler.getProject();
        EventUtil.subscribe(project, this, DataDefinitionChangeListener.TOPIC, dataDefinitionChangeListener);
        EventUtil.subscribe(project, this, SourceCodeManagerListener.TOPIC, sourceCodeManagerListener);
        EventUtil.subscribe(project, this, CompileManagerListener.TOPIC, compileManagerListener);
    }

    private final DataDefinitionChangeListener dataDefinitionChangeListener = new DataDefinitionChangeListener() {
        @Override
        public void dataDefinitionChanged(DBSchema schema, DBObjectType objectType) {
            if (schema.getConnectionHandler() == getConnectionHandler()) {
                DBObjectList childObjectList = schema.getChildObjectList(objectType);
                if (childObjectList != null && childObjectList.isLoaded()) {
                    childObjectList.refresh();
                }

                Set<DBObjectType> childObjectTypes = objectType.getChildren();
                for (DBObjectType childObjectType : childObjectTypes) {
                    DBObjectListContainer childObjects = schema.getChildObjects();
                    if (childObjects != null) {
                        childObjectList = childObjects.getInternalObjectList(childObjectType);
                        if (childObjectList != null && childObjectList.isLoaded()) {
                            childObjectList.refresh();
                        }
                    }
                }
            }
        }

        @Override
        public void dataDefinitionChanged(@NotNull DBSchemaObject schemaObject) {
            if (schemaObject.getConnectionHandler() == getConnectionHandler()) {
                DBObjectListContainer childObjects = schemaObject.getChildObjects();
                if (childObjects != null) {
                    List<DBObjectList<DBObject>> objectLists = childObjects.getObjectLists();
                    if (objectLists != null && !objectLists.isEmpty()) {
                        for (DBObjectList objectList : objectLists) {
                            if (objectList.isLoaded()) {
                                objectList.refresh();
                            }
                        }
                    }
                }
            }
        }
    };

    private final SourceCodeManagerListener sourceCodeManagerListener = new SourceCodeManagerAdapter() {
        @Override
        public void sourceCodeSaved(final DBSourceCodeVirtualFile sourceCodeFile, @Nullable SourceCodeEditor fileEditor) {
            new BackgroundTask(getProject(), "Reloading database object", true) {

                @Override
                protected void execute(@NotNull ProgressIndicator progressIndicator) {
                    DBObject object = sourceCodeFile.getObject();
                    object.refresh();
                }
            }.start();
        }
    };

    private CompileManagerListener compileManagerListener = (connectionHandler, object) -> {
        if (getConnectionHandler().equals(connectionHandler)) {
            refreshObjectsStatus(object);
        }
    };

    public boolean isValid() {
        return connectionConfigHash == getConnectionHandler().getSettings().getDatabaseSettings().hashCode();
    }

    @NotNull
    public ConnectionHandler getConnectionHandler() {
        return FailsafeUtil.get(connectionHandler);
    }

    public List<DBSchema> getSchemas() {
        return schemas.getAllElements();
    }

    @Nullable
    public List<DBUser> getUsers() {
        return DBObjectListImpl.getObjects(users);
    }

    @Nullable
    public List<DBRole> getRoles() {
        return DBObjectListImpl.getObjects(roles);
    }

    @Nullable
    public List<DBSystemPrivilege> getSystemPrivileges() {
        return DBObjectListImpl.getObjects(systemPrivileges);
    }

    @Nullable
    public List<DBCharset> getCharsets() {
        return DBObjectListImpl.getObjects(charsets);
    }

    @NotNull
    public List<DBNativeDataType> getNativeDataTypes(){
        if (nativeDataTypes == null) {
            synchronized (this) {
                if (nativeDataTypes == null) {
                    nativeDataTypes = new ArrayList<>();

                    DatabaseInterfaceProvider interfaceProvider = getConnectionHandler().getInterfaceProvider();
                    List<DataTypeDefinition> dataTypeDefinitions = interfaceProvider.getNativeDataTypes().list();
                    for (DataTypeDefinition dataTypeDefinition : dataTypeDefinitions) {
                        DBNativeDataType dataType = new DBNativeDataType(dataTypeDefinition);
                        nativeDataTypes.add(dataType);
                    }
                    nativeDataTypes.sort((o1, o2) -> -o1.compareTo(o2));
                }
            }
        }
        return nativeDataTypes;
    }

    @Nullable
    public DBNativeDataType getNativeDataType(String name) {
        String upperCaseName = name.toUpperCase();
        for (DBNativeDataType dataType : getNativeDataTypes()) {
            if (upperCaseName.equals(dataType.getName())) {
                return dataType;
            }
        }
        for (DBNativeDataType dataType : getNativeDataTypes()) {
            if (upperCaseName.startsWith(dataType.getName())) {
                return dataType;
            }
        }
        return null;
    }

    @Nullable
    public DBSchema getSchema(String name) {
        return schemas.getObject(name);
    }

    @Nullable
    public DBSchema getPublicSchema() {
        return getSchema("PUBLIC");
    }

    @Nullable
    public DBSchema getUserSchema() {
        for (DBSchema schema : getSchemas()) {
            if (schema.isUserSchema()) return schema;
        }
        return null;
    }

    @Nullable
    public DBUser getUser(String name) {
        return DBObjectListImpl.getObject(users, name);
    }

    @Nullable
    public DBRole getRole(String name) {
        return DBObjectListImpl.getObject(roles, name);
    }

    @Nullable
    @Override
    public DBPrivilege getPrivilege(String name) {
        return DBObjectListImpl.getObject(systemPrivileges, name);
    }

    @Nullable
    public DBSystemPrivilege getSystemPrivilege(String name) {
        return DBObjectListImpl.getObject(systemPrivileges, name);
    }

    @Nullable
    public DBCharset getCharset(String name) {
        return DBObjectListImpl.getObject(charsets, name);
    }

    @NotNull
    @Override
    public List<DBDataType> getCachedDataTypes() {
        return cachedDataTypes;
    }

    /*********************************************************
     *                     TreeElement                       *
     *********************************************************/
    public boolean isTreeStructureLoaded() {
        return treeChildrenLoaded;
    }

    public boolean canExpand() {
        return treeChildrenLoaded && getChildAt(0).isTreeStructureLoaded();
    }

    public int getTreeDepth() {
        return treeParent == null ? 0 : treeParent.getTreeDepth() + 1;
    }

    public BrowserTreeNode getChildAt(int index) {
        return getChildren().get(index);
    }

    @Nullable
    public BrowserTreeNode getParent() {
        return treeParent;
    }

    public List<? extends BrowserTreeNode> getChildren() {
        if (visibleTreeChildren == null) {
            synchronized (this) {
                if (visibleTreeChildren == null) {
                    visibleTreeChildren = new ArrayList<>();
                    visibleTreeChildren.add(new LoadInProgressTreeNode(this));
                    SimpleBackgroundInvocator.invoke(this::buildTreeChildren);
                }
            }
        }
        return visibleTreeChildren;
    }

    private void buildTreeChildren() {
        FailsafeUtil.check(this);
        List<BrowserTreeNode> newTreeChildren = allPossibleTreeChildren;
        Filter<BrowserTreeNode> filter = getConnectionHandler().getObjectTypeFilter();
        if (!filter.acceptsAll(allPossibleTreeChildren)) {
            newTreeChildren = new ArrayList<>();
            for (BrowserTreeNode treeNode : allPossibleTreeChildren) {
                if (treeNode != null && filter.accepts(treeNode)) {
                    DBObjectList objectList = (DBObjectList) treeNode;
                    newTreeChildren.add(objectList);
                }
            }
        }

        for (BrowserTreeNode treeNode : newTreeChildren) {
            DBObjectList objectList = (DBObjectList) treeNode;
            objectList.initTreeElement();
            FailsafeUtil.check(this);
        }

        if (visibleTreeChildren.size() == 1 && visibleTreeChildren.get(0) instanceof LoadInProgressTreeNode) {
            visibleTreeChildren.get(0).dispose();
        }

        visibleTreeChildren = newTreeChildren;
        treeChildrenLoaded = true;

        Project project = FailsafeUtil.get(getProject());
        EventUtil.notify(project, BrowserTreeEventListener.TOPIC).nodeChanged(this, TreeEventType.STRUCTURE_CHANGED);
        DatabaseBrowserManager.scrollToSelectedElement(getConnectionHandler());
    }

    @Override
    public void refreshTreeChildren(@NotNull DBObjectType... objectTypes) {
        if (visibleTreeChildren != null) {
            for (BrowserTreeNode treeNode : visibleTreeChildren) {
                treeNode.refreshTreeChildren(objectTypes);
            }
        }
    }

    public void rebuildTreeChildren() {
        Filter<BrowserTreeNode> filter = getConnectionHandler().getObjectTypeFilter();
        if (visibleTreeChildren != null && DatabaseBrowserUtils.treeVisibilityChanged(allPossibleTreeChildren, visibleTreeChildren, filter)) {
            buildTreeChildren();
        }

        if (visibleTreeChildren != null) {
            for (BrowserTreeNode treeNode : visibleTreeChildren) {
                treeNode.rebuildTreeChildren();
            }
        }
    }

    public int getChildCount() {
        return getChildren().size();
    }

    public boolean isLeaf() {
        return false;
    }

    public int getIndex(BrowserTreeNode child) {
        return getChildren().indexOf(child);
    }

    public Icon getIcon(int flags) {
        return getConnectionHandler().getIcon();
    }

    public String getPresentableText() {
        return getConnectionHandler().getPresentableText();
    }

    public String getPresentableTextDetails() {
        //return getConnectionHandler().isAutoCommit() ? "[Auto Commit]" : null;
        return null;
    }

    public String getPresentableTextConditionalDetails() {
        return null;
    }

    /*********************************************************
     *                  HtmlToolTipBuilder                   *
     *********************************************************/
    public String getToolTip() {
        return new HtmlToolTipBuilder() {
            public void buildToolTip() {
                append(true, "connection", true);
                ConnectionHandler connectionHandler = getConnectionHandler();
                if (connectionHandler.getConnectionStatus().isConnected()) {
                    append(false, " - active", true);
                } else if (connectionHandler.canConnect() && !connectionHandler.isValid()) {
                    append(false, " - invalid", true);
                    append(true, connectionHandler.getConnectionStatus().getStatusMessage(), "-2", "red", false);
                }
                createEmptyRow();

                append(true, connectionHandler.getProject().getName(), false);
                append(false, "/", false);
                append(false, connectionHandler.getName(), false);

                ConnectionPool connectionPool = connectionHandler.getConnectionPool();
                append(true, "Pool size: ", "-2", null, false);
                append(false, String.valueOf(connectionPool.getSize()), false);
                append(false, " (", false);
                append(false, "peak&nbsp;" + connectionPool.getPeakPoolSize(), false);
                append(false, ")", false);
            }
        }.getToolTip();
    }



    /*********************************************************
     *                   NavigationItem                      *
     *********************************************************/
    public void navigate(boolean requestFocus) {
        DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(getProject());
        browserManager.navigateToElement(this, requestFocus, true);
    }
    public boolean canNavigate() {return true;}
    public boolean canNavigateToSource() {return false;}

    public String getName() {
        return getPresentableText();
    }

    public ItemPresentation getPresentation() {
        return this;
    }

    /*********************************************************
     *                   NavigationItem                      *
     *********************************************************/
    public String getLocationString() {
        return null;
    }

    public Icon getIcon(boolean open) {
        return getIcon(0);
    }

    /*********************************************************
     *                 Lookup utilities                      *
     *********************************************************/


    @Nullable
    public DBObject getObject(DatabaseObjectIdentifier objectIdentifier) {
        DBObject object = null;
        for (int i=0; i<objectIdentifier.getObjectTypes().length; i++){
            DBObjectType objectType = objectIdentifier.getObjectTypes()[i];
            String objectName = objectIdentifier.getObjectNames()[i];
            if (object == null) {
                object = getObject(objectType, objectName);
            } else {
                object = object.getChildObject(objectType, objectName, true);
            }
            if (object == null) break;
        }
        return object;
    }

    @Nullable
    public DBObject getObject(DBObjectType objectType, String name) {
        return getObject(objectType, name, 0);
    }

    @Nullable
    public DBObject getObject(DBObjectType objectType, String name, int overload) {
        if (objectType == DBObjectType.SCHEMA) return getSchema(name);
        if (objectType == DBObjectType.USER) return getUser(name);
        if (objectType == DBObjectType.ROLE) return getRole(name);
        if (objectType == DBObjectType.CHARSET) return getCharset(name);
        if (objectType == DBObjectType.SYSTEM_PRIVILEGE) return getSystemPrivilege(name);
        for (DBSchema schema : getSchemas()) {
            if (schema.isPublicSchema() && objectType.isSchemaObject()) {
                DBObject childObject = schema.getChildObject(objectType, name, overload, true);
                if (childObject != null) {
                    return childObject;
                }
            }
        }
        return null;
    }

    private Filter<DBObjectType> getConnectionObjectTypeFilter() {
        return getConnectionHandler().getSettings().getFilterSettings().getObjectTypeFilterSettings().getTypeFilter();
    }

    public void lookupObjectsOfType(LookupConsumer consumer, DBObjectType objectType) throws ConsumerStoppedException {
        if (getConnectionObjectTypeFilter().accepts(objectType)) {
            if (objectType == DBObjectType.SCHEMA) consumer.consume(getSchemas()); else
            if (objectType == DBObjectType.USER) consumer.consume(getUsers()); else
            if (objectType == DBObjectType.ROLE) consumer.consume(getRoles()); else
            if (objectType == DBObjectType.CHARSET) consumer.consume(getCharsets());
            if (objectType == DBObjectType.SYSTEM_PRIVILEGE) consumer.consume(getSystemPrivileges());
        }
    }

    public void lookupChildObjectsOfType(LookupConsumer consumer, DBObject parentObject, DBObjectType objectType, ObjectTypeFilter filter, DBSchema currentSchema) throws ConsumerStoppedException {
        if (getConnectionObjectTypeFilter().accepts(objectType)) {
            if (parentObject != null && currentSchema != null) {
                if (parentObject instanceof DBSchema) {
                    DBSchema schema = (DBSchema) parentObject;
                    if (objectType.isGeneric()) {
                        Set<DBObjectType> concreteTypes = objectType.getInheritingTypes();
                        for (DBObjectType concreteType : concreteTypes) {
                            consumer.check();
                            if (filter.acceptsObject(schema, currentSchema, concreteType)) {
                                consumer.consume(schema.getChildObjects(concreteType));
                            }
                        }
                    } else {
                        if (filter.acceptsObject(schema, currentSchema, objectType)) {
                            consumer.consume(schema.getChildObjects(objectType));
                        }
                    }

                    boolean synonymsSupported = DatabaseCompatibilityInterface.getInstance(parentObject).supportsObjectType(DBObjectType.SYNONYM.getTypeId());
                    if (synonymsSupported && filter.acceptsObject(schema, currentSchema, DBObjectType.SYNONYM)) {
                        for (DBSynonym synonym : schema.getSynonyms()) {
                            consumer.check();
                            DBObject underlyingObject = synonym.getUnderlyingObject();
                            if (underlyingObject != null && underlyingObject.isOfType(objectType)) {
                                consumer.consume(synonym);
                            }
                        }
                    }
                } else {
                    if (objectType.isGeneric()) {
                        Set<DBObjectType> concreteTypes = objectType.getInheritingTypes();
                        for (DBObjectType concreteType : concreteTypes) {
                            consumer.check();
                            if (filter.acceptsRootObject(objectType)) {
                                consumer.consume(parentObject.getChildObjects(concreteType));
                            }
                        }
                    } else {
                        if (filter.acceptsRootObject(objectType)) {
                            consumer.consume(parentObject.getChildObjects(objectType));
                        }
                    }
                }
            }
        }
    }

    public void refreshObjectsStatus(final @Nullable DBSchemaObject requester) {
        if (DatabaseFeature.OBJECT_INVALIDATION.isSupported(getConnectionHandler())) {
            new BackgroundTask(getProject(), "Updating objects status", true, true) {
                @Override
                protected void execute(@NotNull ProgressIndicator progressIndicator) {
                    try {
                        List<DBSchema> schemas = requester == null ? getSchemas() : requester.getReferencingSchemas();

                        int size = schemas.size();
                        for (int i=0; i<size; i++) {
                            if (!progressIndicator.isCanceled()) {
                                DBSchema schema = schemas.get(i);
                                if (size > 3) {
                                    progressIndicator.setIndeterminate(false);
                                    progressIndicator.setFraction(CommonUtil.getProgressPercentage(i, size));
                                }
                                progressIndicator.setText("Updating object status in schema " + schema.getName() + "... ");
                                schema.refreshObjectsStatus();
                            }
                        }
                    } catch (SQLException e) {
                        NotificationUtil.sendErrorNotification(getProject(), "Object Status Refresh", "Could not refresh object status. Cause: " + e.getMessage());
                    }
                }

            }.start();
        }
    }

    public DBObjectListContainer getObjectListContainer() {
        return objectLists;
    }

    @NotNull
    public Project getProject() {
        return getConnectionHandler().getProject();
    }

    @Nullable
    @Override
    public GenericDatabaseElement getParentElement() {
        return null;
    }

    public GenericDatabaseElement getUndisposedElement() {
        return this;
    }

    @Nullable
    public DynamicContent getDynamicContent(DynamicContentType dynamicContentType) {
        if(dynamicContentType instanceof DBObjectType) {
            DBObjectType objectType = (DBObjectType) dynamicContentType;
            DynamicContent dynamicContent = objectLists.getObjectList(objectType);
            if (dynamicContent == null) dynamicContent = objectLists.getInternalObjectList(objectType);
            return dynamicContent;
        }

        if (dynamicContentType instanceof DBObjectRelationType) {
            DBObjectRelationType objectRelationType = (DBObjectRelationType) dynamicContentType;
            return objectRelationLists.getObjectRelationList(objectRelationType);
        }

        return null;
    }

    public void initTreeElement() {}

    @Override
    public String toString() {
        return getConnectionHandler().getName();
    }

    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
            DisposerUtil.disposeInBackground(objectLists);
            DisposerUtil.disposeInBackground(objectRelationLists);
            CollectionUtil.clearCollection(visibleTreeChildren);
            CollectionUtil.clearCollection(allPossibleTreeChildren);
            CollectionUtil.clearCollection(cachedDataTypes);
            treeParent = null;
            connectionHandler = null;
        }
    }

    /*********************************************************
     *                         Loaders                       *
     *********************************************************/
    private static final DynamicContentLoader<DBSchema> SCHEMAS_LOADER = new DynamicContentResultSetLoader<DBSchema>() {
        public ResultSet createResultSet(DynamicContent<DBSchema> dynamicContent, DBNConnection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadSchemas(connection);
        }

        public DBSchema createElement(DynamicContent<DBSchema> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            return new DBSchemaImpl(dynamicContent.getConnectionHandler(), resultSet);
        }
    };

    private static final DynamicContentLoader<DBUser> USERS_LOADER = new DynamicContentResultSetLoader<DBUser>() {
        public ResultSet createResultSet(DynamicContent<DBUser> dynamicContent, DBNConnection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadUsers(connection);
        }

        public DBUser createElement(DynamicContent<DBUser> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            return new DBUserImpl(dynamicContent.getConnectionHandler(), resultSet);
        }
    };

    private static final DynamicContentLoader<DBRole> ROLES_LOADER = new DynamicContentResultSetLoader<DBRole>() {
        public ResultSet createResultSet(DynamicContent<DBRole> dynamicContent, DBNConnection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadRoles(connection);
        }

        public DBRole createElement(DynamicContent<DBRole> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            return new DBRoleImpl(dynamicContent.getConnectionHandler(), resultSet);
        }
    };


    private static final DynamicContentLoader<DBSystemPrivilege> SYSTEM_PRIVILEGES_LOADER = new DynamicContentResultSetLoader<DBSystemPrivilege>() {
        public ResultSet createResultSet(DynamicContent<DBSystemPrivilege> dynamicContent, DBNConnection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadSystemPrivileges(connection);
        }

        public DBSystemPrivilege createElement(DynamicContent<DBSystemPrivilege> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            return new DBSystemPrivilegeImpl(dynamicContent.getConnectionHandler(), resultSet);
        }
    };

    private static final DynamicContentLoader<DBObjectPrivilege> OBJECT_PRIVILEGES_LOADER = new DynamicContentResultSetLoader<DBObjectPrivilege>() {
        public ResultSet createResultSet(DynamicContent<DBObjectPrivilege> dynamicContent, DBNConnection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadObjectPrivileges(connection);
        }

        public DBObjectPrivilege createElement(DynamicContent<DBObjectPrivilege> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            return new DBObjectPrivilegeImpl(dynamicContent.getConnectionHandler(), resultSet);
        }
    };

    private static final DynamicContentLoader<DBCharset> CHARSETS_LOADER = new DynamicContentResultSetLoader<DBCharset>() {
        public ResultSet createResultSet(DynamicContent<DBCharset> dynamicContent, DBNConnection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadCharsets(connection);
        }

        public DBCharset createElement(DynamicContent<DBCharset> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            return new DBCharsetImpl(dynamicContent.getConnectionHandler(), resultSet);
        }
    };

    /*********************************************************
     *                    Relation loaders                   *
     *********************************************************/
    private static final DynamicContentLoader USER_ROLE_RELATION_LOADER = new DynamicContentResultSetLoader() {
        public ResultSet createResultSet(DynamicContent dynamicContent, DBNConnection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadAllUserRoles(connection);
        }

        public DynamicContentElement createElement(DynamicContent dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            String userName = resultSet.getString("USER_NAME");

            DBObjectBundle objectBundle = (DBObjectBundle) dynamicContent.getParentElement();
            DBUser user = objectBundle.getUser(userName);
            if (user != null) {
                DBGrantedRole role = new DBGrantedRoleImpl(user, resultSet);
                return new DBUserRoleRelation(user, role);
            }
            return null;
        }
    };

    private static final DynamicContentLoader USER_PRIVILEGE_RELATION_LOADER = new DynamicContentResultSetLoader() {
        public ResultSet createResultSet(DynamicContent dynamicContent, DBNConnection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadAllUserPrivileges(connection);
        }

        public DynamicContentElement createElement(DynamicContent dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            String userName = resultSet.getString("USER_NAME");

            DBObjectBundle objectBundle = (DBObjectBundle) dynamicContent.getParentElement();
            DBUser user = objectBundle.getUser(userName);
            if (user != null) {
                DBGrantedPrivilege privilege = new DBGrantedPrivilegeImpl(user, resultSet);
                return new DBUserPrivilegeRelation(user, privilege);
            }
            return null;
        }
    };

    private static final DynamicContentLoader ROLE_ROLE_RELATION_LOADER = new DynamicContentResultSetLoader() {
        public ResultSet createResultSet(DynamicContent dynamicContent, DBNConnection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadAllRoleRoles(connection);
        }

        public DynamicContentElement createElement(DynamicContent dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            String roleName = resultSet.getString("ROLE_NAME");

            DBObjectBundle objectBundle = (DBObjectBundle) dynamicContent.getParentElement();
            DBRole role = objectBundle.getRole(roleName);
            if (role != null) {
                DBGrantedRole grantedRole = new DBGrantedRoleImpl(role, resultSet);
                return new DBRoleRoleRelation(role, grantedRole);
            }
            return null;
        }
    };

    private static final DynamicContentLoader ROLE_PRIVILEGE_RELATION_LOADER = new DynamicContentResultSetLoader() {
        public ResultSet createResultSet(DynamicContent dynamicContent, DBNConnection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadAllRolePrivileges(connection);
        }

        public DynamicContentElement createElement(DynamicContent dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            String userName = resultSet.getString("ROLE_NAME");

            DBObjectBundle objectBundle = (DBObjectBundle) dynamicContent.getParentElement();
            DBRole role = objectBundle.getRole(userName);
            if (role != null) {
                DBGrantedPrivilege privilege = new DBGrantedPrivilegeImpl(role, resultSet);
                return new DBRolePrivilegeRelation(role, privilege);
            }
            return null;
        }
    };
}
