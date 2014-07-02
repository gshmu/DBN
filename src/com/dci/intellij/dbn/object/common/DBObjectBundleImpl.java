package com.dci.intellij.dbn.object.common;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.browser.model.BrowserTreeChangeListener;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.model.LoadInProgressTreeNode;
import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.code.sql.color.SQLTextAttributesKeys;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentElement;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.content.loader.DynamicContentResultSetLoader;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.lookup.ConsumerStoppedException;
import com.dci.intellij.dbn.common.lookup.LookupConsumer;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionPool;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.connection.ModuleConnectionBundle;
import com.dci.intellij.dbn.data.type.DBNativeDataType;
import com.dci.intellij.dbn.data.type.DataTypeDefinition;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.database.DatabaseObjectIdentifier;
import com.dci.intellij.dbn.object.DBCharset;
import com.dci.intellij.dbn.object.DBGrantedPrivilege;
import com.dci.intellij.dbn.object.DBGrantedRole;
import com.dci.intellij.dbn.object.DBPrivilege;
import com.dci.intellij.dbn.object.DBRole;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBSynonym;
import com.dci.intellij.dbn.object.DBUser;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectRelationListContainer;
import com.dci.intellij.dbn.object.impl.DBCharsetImpl;
import com.dci.intellij.dbn.object.impl.DBGrantedPrivilegeImpl;
import com.dci.intellij.dbn.object.impl.DBGrantedRoleImpl;
import com.dci.intellij.dbn.object.impl.DBPrivilegeImpl;
import com.dci.intellij.dbn.object.impl.DBRoleImpl;
import com.dci.intellij.dbn.object.impl.DBRolePrivilegeRelation;
import com.dci.intellij.dbn.object.impl.DBRoleRoleRelation;
import com.dci.intellij.dbn.object.impl.DBSchemaImpl;
import com.dci.intellij.dbn.object.impl.DBUserImpl;
import com.dci.intellij.dbn.object.impl.DBUserPrivilegeRelation;
import com.dci.intellij.dbn.object.impl.DBUserRoleRelation;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatus;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class DBObjectBundleImpl implements DBObjectBundle {
    private ConnectionHandler connectionHandler;
    private BrowserTreeNode treeParent;
    private List<BrowserTreeNode> allPossibleTreeChildren;
    private List<BrowserTreeNode> visibleTreeChildren;
    private boolean treeChildrenLoaded;
    private boolean isDisposed;

    private DBObjectList<DBSchema> schemas;
    private DBObjectList<DBUser> users;
    private DBObjectList<DBRole> roles;
    private DBObjectList<DBPrivilege> privileges;
    private DBObjectList<DBCharset> charsets;

    private List<DBNativeDataType> nativeDataTypes;

    protected DBObjectListContainer objectLists;
    protected DBObjectRelationListContainer objectRelationLists;
    private int connectionConfigHash;

    public DBObjectBundleImpl(ConnectionHandler connectionHandler, BrowserTreeNode treeParent) {
        this.connectionHandler = connectionHandler;
        this.treeParent = treeParent;
        connectionConfigHash = connectionHandler.getSettings().getDatabaseSettings().hashCode();

        this.objectLists = new DBObjectListContainer(this);
        users = objectLists.createObjectList(DBObjectType.USER, this, USERS_LOADER, true, false);
        schemas = objectLists.createObjectList(DBObjectType.SCHEMA, this, SCHEMAS_LOADER, new DBObjectList[]{users}, true, false);
        roles = objectLists.createObjectList(DBObjectType.ROLE, this, ROLES_LOADER, true, false);
        privileges = objectLists.createObjectList(DBObjectType.PRIVILEGE, this, PRIVILEGES_LOADER, true, false);
        charsets = objectLists.createObjectList(DBObjectType.CHARSET, this, CHARSETS_LOADER, true, false);
        allPossibleTreeChildren = DatabaseBrowserUtils.createList(schemas, users, roles, privileges, charsets);

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
                users, privileges);

        objectRelationLists.createObjectRelationList(
                DBObjectRelationType.ROLE_ROLE, this,
                "Role role relations",
                ROLE_ROLE_RELATION_LOADER,
                roles);

        objectRelationLists.createObjectRelationList(
                DBObjectRelationType.ROLE_PRIVILEGE, this,
                "Role privilege relations",
                ROLE_PRIVILEGE_RELATION_LOADER,
                roles, privileges);
    }

    public boolean isValid() {
        return connectionConfigHash == connectionHandler.getSettings().getDatabaseSettings().hashCode();
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }

    public List<DBSchema> getSchemas() {
        return schemas.getObjects();
    }

    public List<DBUser> getUsers() {
        return users.getObjects();
    }

    public List<DBRole> getRoles() {
        return roles.getObjects();
    }

    public List<DBPrivilege> getPrivileges() {
        return privileges.getObjects();
    }

    public List<DBCharset> getCharsets() {
        return charsets.getObjects();
    }

    public synchronized List<DBNativeDataType> getNativeDataTypes(){
        if (nativeDataTypes == null) {
            List<DataTypeDefinition> dataTypeDefinitions = connectionHandler.getInterfaceProvider().getNativeDataTypes().list();
            nativeDataTypes = new ArrayList<DBNativeDataType>();
            for (DataTypeDefinition dataTypeDefinition : dataTypeDefinitions) {
                DBNativeDataType dataType = new DBNativeDataType(dataTypeDefinition);
                nativeDataTypes.add(dataType);
            }
            Collections.sort(nativeDataTypes, new Comparator<DBNativeDataType>() {
                @Override
                public int compare(DBNativeDataType o1, DBNativeDataType o2) {
                    return -o1.compareTo(o2);
                }
            });
        }
        return nativeDataTypes;
    }

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

    public DBSchema getSchema(String name) {
        return schemas.getObject(name);
    }

    public DBSchema getPublicSchema() {
        return getSchema("PUBLIC");
    }

    public DBSchema getUserSchema() {
        for (DBSchema schema : getSchemas()) {
            if (schema.isUserSchema()) return schema;
        }
        return null;
    }

    public DBUser getUser(String name) {
        return users.getObject(name);
    }

    public DBRole getRole(String name) {
        return roles.getObject(name);
    }

    public DBPrivilege getPrivilege(String name) {
        return privileges.getObject(name);
    }

    public DBCharset getCharset(String name) {
        return charsets.getObject(name);
    }

    /*********************************************************
     *                     TreeElement                       *
     *********************************************************/
    public boolean isTreeStructureLoaded() {
        return treeChildrenLoaded;
    }

    public boolean canExpand() {
        return treeChildrenLoaded && getTreeChild(0).isTreeStructureLoaded();
    }

    public int getTreeDepth() {
        return treeParent == null ? 0 : treeParent.getTreeDepth() + 1;
    }

    public BrowserTreeNode getTreeChild(int index) {
        return getTreeChildren().get(index);
    }

    public BrowserTreeNode getTreeParent() {
        return treeParent;
    }

    public List<? extends BrowserTreeNode> getTreeChildren() {
        if (visibleTreeChildren == null) {
            visibleTreeChildren = new ArrayList<BrowserTreeNode>();
            visibleTreeChildren.add(new LoadInProgressTreeNode(this));
            new BackgroundTask(getProject(), "Loading data dictionary", true) {
                public void execute(@NotNull ProgressIndicator progressIndicator) {
                    buildTreeChildren();
                }
            }.start();

        }
        return visibleTreeChildren;
    }

    private void buildTreeChildren() {
        List<BrowserTreeNode> newTreeChildren = allPossibleTreeChildren;
        Filter<BrowserTreeNode> filter = connectionHandler.getObjectFilter();
        if (!filter.acceptsAll(allPossibleTreeChildren)) {
            newTreeChildren = new ArrayList<BrowserTreeNode>();
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
        }

        if (visibleTreeChildren.size() == 1 && visibleTreeChildren.get(0) instanceof LoadInProgressTreeNode) {
            visibleTreeChildren.get(0).dispose();
        }

        visibleTreeChildren = newTreeChildren;
        treeChildrenLoaded = true;

        Project project = getProject();
        if (project != null) {
            EventManager.notify(project, BrowserTreeChangeListener.TOPIC).nodeChanged(this, TreeEventType.STRUCTURE_CHANGED);
            new ConditionalLaterInvocator() {
                public void execute() {
                    DatabaseBrowserManager.scrollToSelectedElement(getConnectionHandler());

                }
            }.start();
        }
    }

    public void rebuildTreeChildren() {
        Filter<BrowserTreeNode> filter = connectionHandler.getObjectFilter();
        if (visibleTreeChildren != null && DatabaseBrowserUtils.treeVisibilityChanged(allPossibleTreeChildren, visibleTreeChildren, filter)) {
            buildTreeChildren();
        }

        if (visibleTreeChildren != null) {
            for (BrowserTreeNode treeNode : visibleTreeChildren) {
                treeNode.rebuildTreeChildren();
            }
        }
    }

    public int getTreeChildCount() {
        return getTreeChildren().size();
    }

    public boolean isLeafTreeElement() {
        return false;
    }

    public int getIndexOfTreeChild(BrowserTreeNode child) {
        return getTreeChildren().indexOf(child);
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
                if (getConnectionHandler().getConnectionStatus().isConnected()) {
                    append(false, " - active", true);
                } else if (!getConnectionHandler().isValid()) {
                    append(false, " - invalid", true);
                    append(true, getConnectionHandler().getConnectionStatus().getStatusMessage(), "-2", "red", false);
                }
                createEmptyRow();

                append(true, getConnectionHandler().getProject().getName(), false);
                append(false, "/", false);
                ConnectionBundle connectionBundle = getConnectionHandler().getConnectionBundle();
                if (connectionBundle instanceof ModuleConnectionBundle) {
                    ModuleConnectionBundle moduleConnectionManager = (ModuleConnectionBundle) connectionBundle;
                    append(false, moduleConnectionManager.getModule().getName(), false);
                    append(false, "/", false);
                }

                append(false, getConnectionHandler().getName(), false);

                ConnectionPool connectionPool = getConnectionHandler().getConnectionPool();
                append(true, "Pool size: ", "-2", null, false);
                append(false, "" + connectionPool.getSize(), false);
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
        browserManager.navigateToElement(this, requestFocus);
    }
    public boolean canNavigate() {return true;}
    public boolean canNavigateToSource() {return false;}

    public String getName() {
        return getPresentableText();
    }

    public ItemPresentation getPresentation() {
        return this;
    }

    public FileStatus getFileStatus() {
        return FileStatus.NOT_CHANGED;
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

    public TextAttributesKey getTextAttributesKey() {
        return SQLTextAttributesKeys.IDENTIFIER;
    }

    /*********************************************************
     *                 Lookup utilities                      *
     *********************************************************/


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

    public DBObject getObject(DBObjectType objectType, String name) {
        if (objectType == DBObjectType.SCHEMA) return getSchema(name);
        if (objectType == DBObjectType.USER) return getUser(name);
        if (objectType == DBObjectType.CHARSET) return getCharset(name);
        if (objectType == DBObjectType.PRIVILEGE) return getPrivilege(name);
        for (DBSchema schema : getSchemas()) {
            if (schema.isPublicSchema() && objectType.isSchemaObject()) {
                DBObject childObject = schema.getChildObject(objectType, name, true);
                if (childObject != null) {
                    return childObject;
                }
            }
        }
        return null;
    }

    private Filter<DBObjectType> getConnectionObjectTypeFilter() {
        return connectionHandler.getSettings().getFilterSettings().getObjectTypeFilterSettings().getTypeFilter();
    }

    public void lookupObjectsOfType(LookupConsumer consumer, DBObjectType objectType) throws ConsumerStoppedException {
        if (getConnectionObjectTypeFilter().accepts(objectType)) {
            if (objectType == DBObjectType.SCHEMA) consumer.consume(getSchemas()); else
            if (objectType == DBObjectType.USER) consumer.consume(getUsers()); else
            if (objectType == DBObjectType.CHARSET) consumer.consume(getCharsets());
            if (objectType == DBObjectType.PRIVILEGE) consumer.consume(getPrivileges());
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

    public void refreshObjectsStatus() {
        if (DatabaseCompatibilityInterface.getInstance(getConnectionHandler()).supportsFeature(DatabaseFeature.OBJECT_INVALIDATION)) {
            new BackgroundTask(getProject(), "Updating objects status", true) {
                public void execute(@NotNull ProgressIndicator progressIndicator) {
                    List<DBSchema> schemas = getSchemas();
                    for (int i=0; i<schemas.size(); i++) {
                        DBSchema schema = schemas.get(i);
                        progressIndicator.setText("Updating object status in schema " + schema.getName() + "... ");
                        progressIndicator.setFraction(CommonUtil.getProgressPercentage(i, schemas.size()));
                        schema.refreshObjectsStatus();
                    }
                }

            }.start();
        }
    }

    public DBObjectListContainer getObjectListContainer() {
        return objectLists;
    }

    public Project getProject() {
        return connectionHandler == null ? null : connectionHandler.getProject();
    }

    public GenericDatabaseElement getUndisposedElement() {
        return this;
    }

    public DynamicContent getDynamicContent(DynamicContentType dynamicContentType) {
        if(dynamicContentType instanceof DBObjectType) {
            DBObjectType objectType = (DBObjectType) dynamicContentType;
            DynamicContent dynamicContent = objectLists.getObjectList(objectType);
            if (dynamicContent == null) dynamicContent = objectLists.getHiddenObjectList(objectType);
            return dynamicContent;
        }

        if (dynamicContentType instanceof DBObjectRelationType) {
            DBObjectRelationType objectRelationType = (DBObjectRelationType) dynamicContentType;
            return objectRelationLists.getObjectRelationList(objectRelationType);
        }

        return null;
    }

    public boolean isDisposed() {
        return isDisposed;
    }

    public void initTreeElement() {}

    @Override
    public String toString() {
        return getConnectionHandler().getName();
    }

    public void dispose() {
        if (!isDisposed) {
            isDisposed = true;
            DisposerUtil.dispose(objectLists);
            DisposerUtil.dispose(objectRelationLists);
            CollectionUtil.clearCollection(visibleTreeChildren);
            CollectionUtil.clearCollection(allPossibleTreeChildren);
            treeParent = null;
            connectionHandler = null;
        }
    }

    /*********************************************************
     *                         Loaders                       *
     *********************************************************/
    private static final DynamicContentLoader<DBSchema> SCHEMAS_LOADER = new DynamicContentResultSetLoader<DBSchema>() {
        public ResultSet createResultSet(DynamicContent<DBSchema> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadSchemas(connection);
        }

        public DBSchema createElement(DynamicContent<DBSchema> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            return new DBSchemaImpl(dynamicContent.getConnectionHandler(), resultSet);
        }
    };

    private static final DynamicContentLoader<DBUser> USERS_LOADER = new DynamicContentResultSetLoader<DBUser>() {
        public ResultSet createResultSet(DynamicContent<DBUser> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadUsers(connection);
        }

        public DBUser createElement(DynamicContent<DBUser> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            return new DBUserImpl(dynamicContent.getConnectionHandler(), resultSet);
        }
    };

    private static final DynamicContentLoader<DBRole> ROLES_LOADER = new DynamicContentResultSetLoader<DBRole>() {
        public ResultSet createResultSet(DynamicContent<DBRole> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadRoles(connection);
        }

        public DBRole createElement(DynamicContent<DBRole> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            return new DBRoleImpl(dynamicContent.getConnectionHandler(), resultSet);
        }
    };


    private static final DynamicContentLoader<DBPrivilege> PRIVILEGES_LOADER = new DynamicContentResultSetLoader<DBPrivilege>() {
        public ResultSet createResultSet(DynamicContent<DBPrivilege> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadPrivileges(connection);
        }

        public DBPrivilege createElement(DynamicContent<DBPrivilege> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            return new DBPrivilegeImpl(dynamicContent.getConnectionHandler(), resultSet);
        }
    };

    private static final DynamicContentLoader<DBCharset> CHARSETS_LOADER = new DynamicContentResultSetLoader<DBCharset>() {
        public ResultSet createResultSet(DynamicContent<DBCharset> dynamicContent, Connection connection) throws SQLException {
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
        public ResultSet createResultSet(DynamicContent dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadAllUserRoles(connection);
        }

        public DynamicContentElement createElement(DynamicContent dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            String userName = resultSet.getString("USER_NAME");

            DBObjectBundle objectBundle = (DBObjectBundle) dynamicContent.getParent();
            DBUser user = objectBundle.getUser(userName);
            if (user != null) {
                DBGrantedRole role = new DBGrantedRoleImpl(user, resultSet);
                return new DBUserRoleRelation(user, role);
            }
            return null;
        }
    };

    private static final DynamicContentLoader USER_PRIVILEGE_RELATION_LOADER = new DynamicContentResultSetLoader() {
        public ResultSet createResultSet(DynamicContent dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadAllUserPrivileges(connection);
        }

        public DynamicContentElement createElement(DynamicContent dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            String userName = resultSet.getString("USER_NAME");

            DBObjectBundle objectBundle = (DBObjectBundle) dynamicContent.getParent();
            DBUser user = objectBundle.getUser(userName);
            if (user != null) {
                DBGrantedPrivilege privilege = new DBGrantedPrivilegeImpl(user, resultSet);
                return new DBUserPrivilegeRelation(user, privilege);
            }
            return null;
        }
    };

    private static final DynamicContentLoader ROLE_ROLE_RELATION_LOADER = new DynamicContentResultSetLoader() {
        public ResultSet createResultSet(DynamicContent dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadAllRoleRoles(connection);
        }

        public DynamicContentElement createElement(DynamicContent dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            String roleName = resultSet.getString("ROLE_NAME");

            DBObjectBundle objectBundle = (DBObjectBundle) dynamicContent.getParent();
            DBRole role = objectBundle.getRole(roleName);
            if (role != null) {
                DBGrantedRole grantedRole = new DBGrantedRoleImpl(role, resultSet);
                return new DBRoleRoleRelation(role, grantedRole);
            }
            return null;
        }
    };

    private static final DynamicContentLoader ROLE_PRIVILEGE_RELATION_LOADER = new DynamicContentResultSetLoader() {
        public ResultSet createResultSet(DynamicContent dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadAllRolePrivileges(connection);
        }

        public DynamicContentElement createElement(DynamicContent dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            String userName = resultSet.getString("ROLE_NAME");

            DBObjectBundle objectBundle = (DBObjectBundle) dynamicContent.getParent();
            DBRole role = objectBundle.getRole(userName);
            if (role != null) {
                DBGrantedPrivilege privilege = new DBGrantedPrivilegeImpl(role, resultSet);
                return new DBRolePrivilegeRelation(role, privilege);
            }
            return null;
        }
    };
}
