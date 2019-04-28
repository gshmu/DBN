package com.dci.intellij.dbn.object.common;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.browser.model.BrowserTreeEventListener;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.model.BrowserTreeNodeBase;
import com.dci.intellij.dbn.browser.model.LoadInProgressTreeNode;
import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.code.common.lookup.LookupItemBuilder;
import com.dci.intellij.dbn.code.common.lookup.ObjectLookupItemBuilder;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.content.loader.DynamicContentResultSetLoader;
import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.Nullifiable;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.latent.MapLatent;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.lookup.ConsumerStoppedException;
import com.dci.intellij.dbn.common.lookup.LookupConsumer;
import com.dci.intellij.dbn.common.notification.NotificationSupport;
import com.dci.intellij.dbn.common.thread.Background;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionPool;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.data.type.DBNativeDataType;
import com.dci.intellij.dbn.data.type.DataTypeDefinition;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.database.DatabaseObjectIdentifier;
import com.dci.intellij.dbn.database.common.metadata.def.DBCharsetMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBGrantedPrivilegeMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBGrantedRoleMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBPrivilegeMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBRoleMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBSchemaMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBUserMetadata;
import com.dci.intellij.dbn.editor.code.SourceCodeEditor;
import com.dci.intellij.dbn.editor.code.SourceCodeManagerAdapter;
import com.dci.intellij.dbn.editor.code.SourceCodeManagerListener;
import com.dci.intellij.dbn.execution.compiler.CompileManagerListener;
import com.dci.intellij.dbn.execution.statement.DataDefinitionChangeListener;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.psql.PSQLLanguage;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.dci.intellij.dbn.object.DBCharset;
import com.dci.intellij.dbn.object.DBGrantedPrivilege;
import com.dci.intellij.dbn.object.DBGrantedRole;
import com.dci.intellij.dbn.object.DBObjectPrivilege;
import com.dci.intellij.dbn.object.DBPrivilege;
import com.dci.intellij.dbn.object.DBRole;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBSynonym;
import com.dci.intellij.dbn.object.DBSystemPrivilege;
import com.dci.intellij.dbn.object.DBUser;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectListImpl;
import com.dci.intellij.dbn.object.common.list.DBObjectRelationListContainer;
import com.dci.intellij.dbn.object.impl.DBCharsetImpl;
import com.dci.intellij.dbn.object.impl.DBGrantedPrivilegeImpl;
import com.dci.intellij.dbn.object.impl.DBGrantedRoleImpl;
import com.dci.intellij.dbn.object.impl.DBObjectPrivilegeImpl;
import com.dci.intellij.dbn.object.impl.DBRoleImpl;
import com.dci.intellij.dbn.object.impl.DBRolePrivilegeRelation;
import com.dci.intellij.dbn.object.impl.DBRoleRoleRelation;
import com.dci.intellij.dbn.object.impl.DBSchemaImpl;
import com.dci.intellij.dbn.object.impl.DBSystemPrivilegeImpl;
import com.dci.intellij.dbn.object.impl.DBUserImpl;
import com.dci.intellij.dbn.object.impl.DBUserPrivilegeRelation;
import com.dci.intellij.dbn.object.impl.DBUserRoleRelation;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.vfs.file.DBObjectVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.dci.intellij.dbn.object.common.DBObjectRelationType.*;
import static com.dci.intellij.dbn.object.common.DBObjectType.*;

@Nullifiable
public class DBObjectBundleImpl extends BrowserTreeNodeBase implements DBObjectBundle, NotificationSupport {
    private ConnectionHandlerRef connectionHandlerRef;
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

    private Latent<List<DBNativeDataType>> nativeDataTypes = Latent.basic(() -> computeNativeDataTypes());
    private List<DBDataType> cachedDataTypes = CollectionUtil.createConcurrentList();

    private DBObjectListContainer objectLists;
    private DBObjectRelationListContainer objectRelationLists;
    private int connectionConfigHash;

    private MapLatent<DBObjectRef, LookupItemBuilder> sqlLookupItemBuilders =
            MapLatent.create((objectRef) ->
                    new ObjectLookupItemBuilder(objectRef, SQLLanguage.INSTANCE));

    private MapLatent<DBObjectRef, LookupItemBuilder> psqlLookupItemBuilders =
            MapLatent.create((objectRef) ->
                    new ObjectLookupItemBuilder(objectRef, PSQLLanguage.INSTANCE));

    private MapLatent<DBObjectRef, DBObjectPsiFacade> objectPsiFacades =
            MapLatent.create((objectRef) ->
                    new DBObjectPsiFacade(objectRef));

    private MapLatent<DBObjectRef, DBObjectVirtualFile> virtualFiles =
            MapLatent.create((objectRef) ->
                    new DBObjectVirtualFile(getProject(), objectRef));


    private Latent<PsiFile> fakeObjectFile = Latent.basic(() -> {
        PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(getProject());
        return psiFileFactory.createFileFromText("object", SQLLanguage.INSTANCE, "");
    });

    public DBObjectBundleImpl(ConnectionHandler connectionHandler, BrowserTreeNode treeParent) {
        this.connectionHandlerRef = ConnectionHandlerRef.from(connectionHandler);
        this.treeParent = treeParent;
        connectionConfigHash = connectionHandler.getSettings().getDatabaseSettings().hashCode();

        objectLists = new DBObjectListContainer(this);
        users = objectLists.createObjectList(USER, this);
        schemas = objectLists.createObjectList(SCHEMA, this);
        roles = objectLists.createObjectList(ROLE, this);
        systemPrivileges = objectLists.createObjectList(SYSTEM_PRIVILEGE, this);
        charsets = objectLists.createObjectList(CHARSET, this);
        allPossibleTreeChildren = DatabaseBrowserUtils.createList(schemas, users, roles, systemPrivileges, charsets);

        objectRelationLists = new DBObjectRelationListContainer(this);
        objectRelationLists.createObjectRelationList(
                USER_ROLE, this,
                users, roles);

        objectRelationLists.createObjectRelationList(
                USER_PRIVILEGE, this,
                users, systemPrivileges);

        objectRelationLists.createObjectRelationList(
                ROLE_ROLE, this,
                roles);

        objectRelationLists.createObjectRelationList(
                ROLE_PRIVILEGE, this,
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
                schema.refresh(objectType);
                objectType.getChildren().forEach(
                        childObjectType -> schema.refresh(childObjectType));
            }
        }

        @Override
        public void dataDefinitionChanged(@NotNull DBSchemaObject schemaObject) {
            if (schemaObject.getConnectionHandler() == getConnectionHandler()) {
                schemaObject.refresh();
            }
        }
    };

    private final SourceCodeManagerListener sourceCodeManagerListener = new SourceCodeManagerAdapter() {
        @Override
        public void sourceCodeSaved(DBSourceCodeVirtualFile sourceCodeFile, @Nullable SourceCodeEditor fileEditor) {
            if (sourceCodeFile.getConnectionId() == getConnectionId()) {
                Progress.background(getProject(), "Reloading database object", false,
                        (progress) -> {
                            DBObject object = sourceCodeFile.getObject();
                            object.refresh();
                        });
            }
        }
    };

    private CompileManagerListener compileManagerListener = (connectionHandler, object) -> {
        if (getConnectionHandler().equals(connectionHandler)) {
            refreshObjectsStatus(object);
        }
    };

    @Override
    public LookupItemBuilder getLookupItemBuilder(DBObjectRef objectRef, DBLanguage language) {
        if (language == SQLLanguage.INSTANCE) {
            return sqlLookupItemBuilders.get(objectRef);
        }
        if (language == PSQLLanguage.INSTANCE) {
            return psqlLookupItemBuilders.get(objectRef);
        }
        return null;
    }

    @Override
    public DBObjectPsiFacade getObjectPsiFacade(DBObjectRef objectRef) {
        return objectPsiFacades.get(objectRef);
    }

    @Override
    public DBObjectVirtualFile getObjectVirtualFile(DBObjectRef objectRef) {
        return virtualFiles.get(objectRef);
    }

    @Override
    public PsiFile getFakeObjectFile() {
        return fakeObjectFile.get();
    }

    @Override
    public boolean isValid() {
        return connectionConfigHash == getConnectionHandler().getSettings().getDatabaseSettings().hashCode();
    }

    @NotNull
    @Override
    public ConnectionId getConnectionId() {
        return connectionHandlerRef.getConnectionId();
    }

    @Override
    @NotNull
    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.ensure();
    }

    @Override
    public List<DBSchema> getSchemas() {
        return Failsafe.nn(schemas).getAllElements();
    }

    @Override
    @Nullable
    public List<DBUser> getUsers() {
        return DBObjectListImpl.getObjects(users);
    }

    @Override
    @Nullable
    public List<DBRole> getRoles() {
        return DBObjectListImpl.getObjects(roles);
    }

    @Override
    @Nullable
    public List<DBSystemPrivilege> getSystemPrivileges() {
        return DBObjectListImpl.getObjects(systemPrivileges);
    }

    @Override
    @Nullable
    public List<DBCharset> getCharsets() {
        return DBObjectListImpl.getObjects(charsets);
    }

    @Override
    @NotNull
    public List<DBNativeDataType> getNativeDataTypes(){
        return nativeDataTypes.get();
    }

    private List<DBNativeDataType> computeNativeDataTypes() {
        List<DBNativeDataType> nativeDataTypes = new ArrayList<>();

        DatabaseInterfaceProvider interfaceProvider = getConnectionHandler().getInterfaceProvider();
        List<DataTypeDefinition> dataTypeDefinitions = interfaceProvider.getNativeDataTypes().list();
        for (DataTypeDefinition dataTypeDefinition : dataTypeDefinitions) {
            DBNativeDataType dataType = new DBNativeDataType(dataTypeDefinition);
            nativeDataTypes.add(dataType);
        }
        nativeDataTypes.sort((o1, o2) -> -o1.compareTo(o2));
        return nativeDataTypes;
    }

    @Override
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

    @Override
    @Nullable
    public DBSchema getSchema(String name) {
        return Failsafe.nn(schemas).getObject(name);
    }

    @Override
    @Nullable
    public DBSchema getPublicSchema() {
        return getSchema("PUBLIC");
    }

    @Override
    @Nullable
    public DBSchema getUserSchema() {
        for (DBSchema schema : getSchemas()) {
            if (schema.isUserSchema()) return schema;
        }
        return null;
    }

    @Override
    @Nullable
    public DBUser getUser(String name) {
        return DBObjectListImpl.getObject(users, name);
    }

    @Override
    @Nullable
    public DBRole getRole(String name) {
        return DBObjectListImpl.getObject(roles, name);
    }

    @Nullable
    @Override
    public DBPrivilege getPrivilege(String name) {
        return DBObjectListImpl.getObject(systemPrivileges, name);
    }

    @Override
    @Nullable
    public DBSystemPrivilege getSystemPrivilege(String name) {
        return DBObjectListImpl.getObject(systemPrivileges, name);
    }

    @Override
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
    @Override
    public boolean isTreeStructureLoaded() {
        return treeChildrenLoaded;
    }

    @Override
    public boolean canExpand() {
        return treeChildrenLoaded && getChildAt(0).isTreeStructureLoaded();
    }

    @Override
    public int getTreeDepth() {
        return treeParent == null ? 0 : treeParent.getTreeDepth() + 1;
    }

    @Override
    public BrowserTreeNode getChildAt(int index) {
        return getChildren().get(index);
    }

    @Override
    @Nullable
    public BrowserTreeNode getParent() {
        return treeParent;
    }

    @Override
    public List<? extends BrowserTreeNode> getChildren() {
        if (visibleTreeChildren == null) {
            synchronized (this) {
                if (visibleTreeChildren == null) {
                    visibleTreeChildren = new ArrayList<>();
                    visibleTreeChildren.add(new LoadInProgressTreeNode(this));

                    Background.run(() -> buildTreeChildren());
                }
            }
        }
        return visibleTreeChildren;
    }

    private void buildTreeChildren() {
        checkDisposed();
        ConnectionHandler connectionHandler = getConnectionHandler();
        Filter<BrowserTreeNode> objectTypeFilter = connectionHandler.getObjectTypeFilter();

        List<BrowserTreeNode> treeChildren = CollectionUtil.filter(allPossibleTreeChildren, false, true, objectTypeFilter);
        treeChildren = CommonUtil.nvl(treeChildren, Collections.emptyList());

        treeChildren.forEach(objectList -> {
            objectList.initTreeElement();
            checkDisposed();});

        if (visibleTreeChildren.size() == 1 && visibleTreeChildren.get(0) instanceof LoadInProgressTreeNode) {
            visibleTreeChildren.get(0).dispose();
        }

        visibleTreeChildren = treeChildren;
        treeChildrenLoaded = true;

        EventUtil.notify(getProject(),
                BrowserTreeEventListener.TOPIC,
                (listener) -> listener.nodeChanged(this, TreeEventType.STRUCTURE_CHANGED));

        DatabaseBrowserManager.scrollToSelectedElement(connectionHandler);
    }

    @Override
    public void refreshTreeChildren(@NotNull DBObjectType... objectTypes) {
        CollectionUtil.forEach(
                visibleTreeChildren,
                treeNode -> treeNode.refreshTreeChildren(objectTypes));
    }

    @Override
    public void rebuildTreeChildren() {
        Filter<BrowserTreeNode> filter = getConnectionHandler().getObjectTypeFilter();
        if (visibleTreeChildren != null && DatabaseBrowserUtils.treeVisibilityChanged(allPossibleTreeChildren, visibleTreeChildren, filter)) {
            buildTreeChildren();
        }

        CollectionUtil.forEach(
                visibleTreeChildren,
                treeNode -> treeNode.rebuildTreeChildren());
    }

    @Override
    public int getChildCount() {
        return getChildren().size();
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public int getIndex(BrowserTreeNode child) {
        return getChildren().indexOf(child);
    }

    @Override
    public Icon getIcon(int flags) {
        return getConnectionHandler().getIcon();
    }

    @Override
    public String getPresentableText() {
        return getConnectionHandler().getPresentableText();
    }

    @Override
    public String getPresentableTextDetails() {
        //return getConnectionHandler().isAutoCommit() ? "[Auto Commit]" : null;
        return null;
    }

    @Override
    public String getPresentableTextConditionalDetails() {
        return null;
    }

    /*********************************************************
     *                  HtmlToolTipBuilder                   *
     *********************************************************/
    @Override
    public String getToolTip() {
        return new HtmlToolTipBuilder() {
            @Override
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
    @Override
    public void navigate(boolean requestFocus) {
        DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(getProject());
        browserManager.navigateToElement(this, requestFocus, true);
    }
    @Override
    public boolean canNavigate() {return true;}
    @Override
    public boolean canNavigateToSource() {return false;}

    @NotNull
    @Override
    public String getName() {
        return getPresentableText();
    }

    @Override
    public ItemPresentation getPresentation() {
        return this;
    }

    /*********************************************************
     *                   NavigationItem                      *
     *********************************************************/
    @Override
    public String getLocationString() {
        return null;
    }

    @Override
    public Icon getIcon(boolean open) {
        return getIcon(0);
    }

    /*********************************************************
     *                 Lookup utilities                      *
     *********************************************************/


    @Override
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

    @Override
    @Nullable
    public DBObject getObject(DBObjectType objectType, String name) {
        return getObject(objectType, name, 0);
    }

    @Override
    @Nullable
    public DBObject getObject(DBObjectType objectType, String name, int overload) {
        if (objectType == SCHEMA) return getSchema(name);
        if (objectType == USER) return getUser(name);
        if (objectType == ROLE) return getRole(name);
        if (objectType == CHARSET) return getCharset(name);
        if (objectType == SYSTEM_PRIVILEGE) return getSystemPrivilege(name);
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

    @Override
    public void lookupObjectsOfType(LookupConsumer consumer, DBObjectType objectType) throws ConsumerStoppedException {
        if (getConnectionObjectTypeFilter().accepts(objectType)) {
            if (objectType == SCHEMA) consumer.consume(getSchemas()); else
            if (objectType == USER) consumer.consume(getUsers()); else
            if (objectType == ROLE) consumer.consume(getRoles()); else
            if (objectType == CHARSET) consumer.consume(getCharsets());
            if (objectType == SYSTEM_PRIVILEGE) consumer.consume(getSystemPrivileges());
        }
    }

    @Override
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

                    boolean synonymsSupported = DatabaseCompatibilityInterface.getInstance(parentObject).supportsObjectType(SYNONYM.getTypeId());
                    if (synonymsSupported && filter.acceptsObject(schema, currentSchema, SYNONYM)) {
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

    @Override
    public void refreshObjectsStatus(final @Nullable DBSchemaObject requester) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        if (DatabaseFeature.OBJECT_INVALIDATION.isSupported(connectionHandler)) {
            Project project = getProject();
            Progress.background(project, "Updating objects status", true,
                    (progress) -> {
                        try {
                            List<DBSchema> schemas = requester == null ? getSchemas() : requester.getReferencingSchemas();

                            int size = schemas.size();
                            for (int i = 0; i < size; i++) {
                                ProgressMonitor.checkCancelled();

                                DBSchema schema = schemas.get(i);
                                if (size > 3) {
                                    progress.setIndeterminate(false);
                                    progress.setFraction(CommonUtil.getProgressPercentage(i, size));
                                }
                                progress.setText("Updating object status in schema " + schema.getName() + "... ");
                                schema.refreshObjectsStatus();
                            }
                        } catch (SQLException e) {
                            sendErrorNotification("Object Status Refresh", "Could not refresh object status. Cause: " + e.getMessage());
                        }
                    });
        }
    }

    @Override
    public DBObjectListContainer getObjectListContainer() {
        return Failsafe.nn(objectLists);
    }

    @Override
    @NotNull
    public Project getProject() {
        return getConnectionHandler().getProject();
    }

    @Nullable
    @Override
    public GenericDatabaseElement getParentElement() {
        return null;
    }

    @Override
    public GenericDatabaseElement getUndisposedElement() {
        return this;
    }

    @Override
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

    @Override
    public void initTreeElement() {}

    @Override
    public String toString() {
        return getConnectionHandler().getName();
    }

    @Override
    public void disposeInner() {
        Disposer.disposeInBackground(
                objectLists,
                objectRelationLists,
                sqlLookupItemBuilders,
                psqlLookupItemBuilders,
                objectPsiFacades,
                virtualFiles);
        super.disposeInner();
    }

    /*********************************************************
     *                         Loaders                       *
     *********************************************************/
    static {
        new DynamicContentResultSetLoader<DBSchema, DBSchemaMetadata>(null, SCHEMA, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBSchema> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                return metadataInterface.loadSchemas(connection);
            }

            @Override
            public DBSchema createElement(DynamicContent<DBSchema> content, DBSchemaMetadata metadata, LoaderCache cache) throws SQLException {
                return new DBSchemaImpl(content.getConnectionHandler(), metadata);
            }
        };

        new DynamicContentResultSetLoader<DBUser, DBUserMetadata>(null, USER, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBUser> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                return metadataInterface.loadUsers(connection);
            }

            @Override
            public DBUser createElement(DynamicContent<DBUser> content, DBUserMetadata metadata, LoaderCache cache) throws SQLException {
                return new DBUserImpl(content.getConnectionHandler(), metadata);
            }
        };

        new DynamicContentResultSetLoader<DBRole, DBRoleMetadata>(null, ROLE, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBRole> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                return metadataInterface.loadRoles(connection);
            }

            @Override
            public DBRole createElement(DynamicContent<DBRole> content, DBRoleMetadata metadata, LoaderCache cache) throws SQLException {
                return new DBRoleImpl(content.getConnectionHandler(), metadata);
            }
        };

        new DynamicContentResultSetLoader<DBSystemPrivilege, DBPrivilegeMetadata>(null, SYSTEM_PRIVILEGE, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBSystemPrivilege> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                return metadataInterface.loadSystemPrivileges(connection);
            }

            @Override
            public DBSystemPrivilege createElement(DynamicContent<DBSystemPrivilege> content, DBPrivilegeMetadata metadata, LoaderCache cache) throws SQLException {
                return new DBSystemPrivilegeImpl(content.getConnectionHandler(), metadata);
            }
        };

        new DynamicContentResultSetLoader<DBObjectPrivilege, DBPrivilegeMetadata>(null, OBJECT_PRIVILEGE, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBObjectPrivilege> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                return metadataInterface.loadObjectPrivileges(connection);
            }

            @Override
            public DBObjectPrivilege createElement(DynamicContent<DBObjectPrivilege> content, DBPrivilegeMetadata metadata, LoaderCache cache) throws SQLException {
                return new DBObjectPrivilegeImpl(content.getConnectionHandler(), metadata);
            }
        };

        new DynamicContentResultSetLoader<DBCharset, DBCharsetMetadata>(null, CHARSET, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBCharset> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                return metadataInterface.loadCharsets(connection);
            }

            @Override
            public DBCharset createElement(DynamicContent<DBCharset> content, DBCharsetMetadata metadata, LoaderCache cache) throws SQLException {
                return new DBCharsetImpl(content.getConnectionHandler(), metadata);
            }
        };

        new DynamicContentResultSetLoader<DBUserRoleRelation, DBGrantedRoleMetadata>(null, USER_ROLE, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                return metadataInterface.loadAllUserRoles(connection);
            }

            @Override
            public DBUserRoleRelation createElement(DynamicContent<DBUserRoleRelation> content, DBGrantedRoleMetadata metadata, LoaderCache cache) throws SQLException {
                String userName = metadata.getUserName();

                DBObjectBundle objectBundle = (DBObjectBundle) content.getParentElement();
                DBUser user = objectBundle.getUser(userName);
                if (user != null) {
                    DBGrantedRole role = new DBGrantedRoleImpl(user, metadata);
                    return new DBUserRoleRelation(user, role);
                }
                return null;
            }
        };

        new DynamicContentResultSetLoader<DBUserPrivilegeRelation, DBGrantedPrivilegeMetadata>(null, USER_PRIVILEGE, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                return metadataInterface.loadAllUserPrivileges(connection);
            }

            @Override
            public DBUserPrivilegeRelation createElement(DynamicContent<DBUserPrivilegeRelation> content, DBGrantedPrivilegeMetadata metadata, LoaderCache cache) throws SQLException {
                String userName = metadata.getUserName();

                DBObjectBundle objectBundle = (DBObjectBundle) content.getParentElement();
                DBUser user = objectBundle.getUser(userName);
                if (user != null) {
                    DBGrantedPrivilege privilege = new DBGrantedPrivilegeImpl(user, metadata);
                    return new DBUserPrivilegeRelation(user, privilege);
                }
                return null;
            }
        };

        new DynamicContentResultSetLoader<DBRoleRoleRelation, DBGrantedRoleMetadata>(null, ROLE_ROLE, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                return metadataInterface.loadAllRoleRoles(connection);
            }

            @Override
            public DBRoleRoleRelation createElement(DynamicContent<DBRoleRoleRelation> content, DBGrantedRoleMetadata metadata, LoaderCache cache) throws SQLException {
                String roleName = metadata.getRoleName();

                DBObjectBundle objectBundle = (DBObjectBundle) content.getParentElement();
                DBRole role = objectBundle.getRole(roleName);
                if (role != null) {
                    DBGrantedRole grantedRole = new DBGrantedRoleImpl(role, metadata);
                    return new DBRoleRoleRelation(role, grantedRole);
                }
                return null;
            }
        };

        new DynamicContentResultSetLoader<DBRolePrivilegeRelation, DBGrantedPrivilegeMetadata>(null, ROLE_PRIVILEGE, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                return metadataInterface.loadAllRolePrivileges(connection);
            }

            @Override
            public DBRolePrivilegeRelation createElement(DynamicContent<DBRolePrivilegeRelation> content, DBGrantedPrivilegeMetadata metadata, LoaderCache cache) throws SQLException {
                String userName = metadata.getRoleName();

                DBObjectBundle objectBundle = (DBObjectBundle) content.getParentElement();
                DBRole role = objectBundle.getRole(userName);
                if (role != null) {
                    DBGrantedPrivilege privilege = new DBGrantedPrivilegeImpl(role, metadata);
                    return new DBRolePrivilegeRelation(role, privilege);
                }
                return null;
            }
        };
    }
}
