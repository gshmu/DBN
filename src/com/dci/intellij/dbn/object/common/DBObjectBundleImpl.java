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
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoaderImpl;
import com.dci.intellij.dbn.common.content.loader.DynamicContentResultSetLoader;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.SafeDisposer;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.notification.NotificationGroup;
import com.dci.intellij.dbn.common.notification.NotificationSupport;
import com.dci.intellij.dbn.common.thread.Background;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.thread.Read;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.util.Commons;
import com.dci.intellij.dbn.common.util.Consumer;
import com.dci.intellij.dbn.common.util.Lists;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionPool;
import com.dci.intellij.dbn.connection.ConnectionRef;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.data.type.DBDataTypeBundle;
import com.dci.intellij.dbn.data.type.DBNativeDataType;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.database.DatabaseObjectIdentifier;
import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadata;
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
import com.dci.intellij.dbn.object.DBConsole;
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
import com.dci.intellij.dbn.object.type.DBObjectRelationType;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.dci.intellij.dbn.vfs.file.DBObjectVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.dci.intellij.dbn.browser.DatabaseBrowserUtils.treeVisibilityChanged;
import static com.dci.intellij.dbn.common.content.DynamicContentProperty.*;
import static com.dci.intellij.dbn.object.type.DBObjectRelationType.*;
import static com.dci.intellij.dbn.object.type.DBObjectType.*;

public class DBObjectBundleImpl extends BrowserTreeNodeBase implements DBObjectBundle, NotificationSupport {
    private final ConnectionRef connection;
    private final BrowserTreeNode treeParent;
    private final List<BrowserTreeNode> allPossibleTreeChildren;
    private volatile List<BrowserTreeNode> visibleTreeChildren;
    private boolean treeChildrenLoaded;

    private final DBObjectList<DBConsole> consoles;
    private final DBObjectList<DBSchema> schemas;
    private final DBObjectList<DBUser> users;
    private final DBObjectList<DBRole> roles;
    private final DBObjectList<DBSystemPrivilege> systemPrivileges;
    private final DBObjectList<DBObjectPrivilege> objectPrivileges = null; // TODO
    private final DBObjectList<DBCharset> charsets;

    private final DBDataTypeBundle dataTypes;

    private final DBObjectListContainer objectLists;
    private final long configSignature;

    private final Map<DBObjectRef<?>, LookupItemBuilder> sqlLookupItemBuilders = new ConcurrentHashMap<>();
    private final Map<DBObjectRef<?>, LookupItemBuilder> psqlLookupItemBuilders = new ConcurrentHashMap<>();
    private final Map<DBObjectRef<?>, DBObjectPsiFacade> objectPsiFacades = new ConcurrentHashMap<>();
    private final Map<DBObjectRef<?>, DBObjectVirtualFile<?>> virtualFiles = new ConcurrentHashMap<>();

    private final PsiFile fakeObjectFile;

    private final Latent<List<DBSchema>> publicSchemas;

    public DBObjectBundleImpl(ConnectionHandler connection, BrowserTreeNode treeParent) {
        this.connection = ConnectionRef.of(connection);
        this.dataTypes = new DBDataTypeBundle(connection);
        this.treeParent = treeParent;
        this.configSignature = connection.getSettings().getDatabaseSettings().getSignature();

        this.objectLists = new DBObjectListContainer(this);
        this.consoles = objectLists.createObjectList(CONSOLE, this, PASSIVE);
        this.users = objectLists.createObjectList(USER, this);
        this.schemas = objectLists.createObjectList(SCHEMA, this);
        this.roles = objectLists.createObjectList(ROLE, this);
        this.systemPrivileges = objectLists.createObjectList(SYSTEM_PRIVILEGE, this);
        this.charsets = objectLists.createObjectList(CHARSET, this);
        this.allPossibleTreeChildren = DatabaseBrowserUtils.createList(consoles, schemas, users, roles, systemPrivileges, charsets);

        this.objectLists.createObjectRelationList(USER_ROLE, this, users, roles, INTERNAL, GROUPED);
        this.objectLists.createObjectRelationList(USER_PRIVILEGE, this, users, systemPrivileges, INTERNAL, GROUPED);
        this.objectLists.createObjectRelationList(ROLE_ROLE, this, roles, roles, INTERNAL, GROUPED);
        this.objectLists.createObjectRelationList(ROLE_PRIVILEGE, this, roles, systemPrivileges, INTERNAL, GROUPED);

        Project project = connection.getProject();
        PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(project);
        this.fakeObjectFile = Read.call(() -> psiFileFactory.createFileFromText("object", SQLLanguage.INSTANCE, ""));


        this.publicSchemas = Latent.mutable(
                () -> Failsafe.nd(schemas).getSignature(),
                () -> Lists.filter(getSchemas(), s -> s.isPublicSchema()));

        ProjectEvents.subscribe(project, this, DataDefinitionChangeListener.TOPIC, dataDefinitionChangeListener);
        ProjectEvents.subscribe(project, this, SourceCodeManagerListener.TOPIC, sourceCodeManagerListener);
        ProjectEvents.subscribe(project, this, CompileManagerListener.TOPIC, compileManagerListener);

        Disposer.register(connection, this);
    }

    private final DataDefinitionChangeListener dataDefinitionChangeListener = new DataDefinitionChangeListener() {
        @Override
        public void dataDefinitionChanged(DBSchema schema, DBObjectType objectType) {
            if (schema.getConnection() == DBObjectBundleImpl.this.getConnection()) {
                schema.refresh(objectType);
                for (DBObjectType childObjectType : objectType.getChildren()) {
                    schema.refresh(childObjectType);
                }
            }
        }

        @Override
        public void dataDefinitionChanged(@NotNull DBSchemaObject schemaObject) {
            if (schemaObject.getConnection() == DBObjectBundleImpl.this.getConnection()) {
                schemaObject.refresh();
            }
        }
    };

    private final SourceCodeManagerListener sourceCodeManagerListener = new SourceCodeManagerAdapter() {
        @Override
        public void sourceCodeSaved(@NotNull DBSourceCodeVirtualFile sourceCodeFile, @Nullable SourceCodeEditor fileEditor) {
            if (sourceCodeFile.getConnectionId() == getConnectionId()) {
                Progress.background(getProject(), "Reloading database object", false, progress -> {
                    DBObject object = sourceCodeFile.getObject();
                    object.refresh();
                });
            }
        }
    };

    private final CompileManagerListener compileManagerListener = (connection, object) -> {
        if (this.getConnection().equals(connection)) {
            refreshObjectsStatus(object);
        }
    };

    @Override
    public LookupItemBuilder getLookupItemBuilder(DBObjectRef<?> objectRef, DBLanguage<?> language) {
        if (language == SQLLanguage.INSTANCE) {
            return sqlLookupItemBuilders.computeIfAbsent(objectRef, r ->  new ObjectLookupItemBuilder(r, SQLLanguage.INSTANCE));
        }
        if (language == PSQLLanguage.INSTANCE) {
            return psqlLookupItemBuilders.computeIfAbsent(objectRef, r -> new ObjectLookupItemBuilder(r, PSQLLanguage.INSTANCE));
        }
        return null;
    }

    @Override
    public DBObjectPsiFacade getObjectPsiFacade(DBObjectRef<?> objectRef) {
        return objectPsiFacades.computeIfAbsent(objectRef, r -> new DBObjectPsiFacade(r));
    }

    @Override
    public DBObjectVirtualFile<?> getObjectVirtualFile(DBObjectRef<?> objectRef) {
        return virtualFiles.computeIfAbsent(objectRef, r -> new DBObjectVirtualFile<>(getProject(), r));
    }

    @Override
    public PsiFile getFakeObjectFile() {
        return fakeObjectFile;
    }

    @Override
    public boolean isValid() {
        return configSignature == this.getConnection().getSettings().getDatabaseSettings().getSignature();
    }

    @NotNull
    @Override
    public ConnectionId getConnectionId() {
        return connection.getConnectionId();
    }

    @Override
    @NotNull
    public ConnectionHandler getConnection() {
        return connection.ensure();
    }

    @Override
    public List<DBConsole> getConsoles() {
        return this.getConnection().getConsoleBundle().getConsoles();
    }

    @Override
    public List<DBSchema> getSchemas() {
        return Failsafe.nn(schemas).getAllElements();
    }

    @Override
    public List<DBSchema> getPublicSchemas() {
        return publicSchemas.get();
    }

    @Override
    public List<SchemaId> getSchemaIds() {
        return Lists.convert(getSchemas(), schema -> SchemaId.get(schema.getName()));
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
    @Nullable
    public DBNativeDataType getNativeDataType(String name) {
        return dataTypes.getNativeDataType(name);
    }

    @NotNull
    @Override
    public DBDataTypeBundle getDataTypes() {
        return dataTypes;
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
        ConnectionHandler connection = this.getConnection();
        Filter<BrowserTreeNode> objectTypeFilter = connection.getObjectTypeFilter();

        List<BrowserTreeNode> treeChildren = Lists.filter(allPossibleTreeChildren, objectTypeFilter);
        treeChildren = Commons.nvl(treeChildren, Collections.emptyList());

        for (BrowserTreeNode objectList : treeChildren) {
            Progress.background(
                    getProject(),
                    this.getConnection().getMetaLoadTitle(),
                    true,
                    progress -> objectList.initTreeElement());
            checkDisposed();
        }

        if (visibleTreeChildren.size() == 1 && visibleTreeChildren.get(0) instanceof LoadInProgressTreeNode) {
            visibleTreeChildren.get(0).dispose();
        }

        visibleTreeChildren = treeChildren;
        treeChildrenLoaded = true;

        ProjectEvents.notify(getProject(),
                BrowserTreeEventListener.TOPIC,
                (listener) -> listener.nodeChanged(this, TreeEventType.STRUCTURE_CHANGED));

        DatabaseBrowserManager.scrollToSelectedElement(connection);
    }

    @Override
    public void refreshTreeChildren(@NotNull DBObjectType... objectTypes) {
        if (visibleTreeChildren != null) {
            for (BrowserTreeNode treeNode : visibleTreeChildren) {
                treeNode.refreshTreeChildren(objectTypes);
            }
        }
    }

    @Override
    public void rebuildTreeChildren() {
        if (visibleTreeChildren != null) {
            Filter<BrowserTreeNode> filter = this.getConnection().getObjectTypeFilter();
            if (treeVisibilityChanged(allPossibleTreeChildren, visibleTreeChildren, filter)) {
                buildTreeChildren();
            }
            for (BrowserTreeNode treeNode : visibleTreeChildren) {
                treeNode.rebuildTreeChildren();
            }
        }
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
        return this.getConnection().getIcon();
    }

    @Override
    public String getPresentableText() {
        return this.getConnection().getPresentableText();
    }

    @Override
    public String getPresentableTextDetails() {
        //return getCache().isAutoCommit() ? "[Auto Commit]" : null;
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
                ConnectionHandler connection = DBObjectBundleImpl.this.getConnection();
                if (connection.getConnectionStatus().isConnected()) {
                    append(false, " - active", true);
                } else if (connection.canConnect() && !connection.isValid()) {
                    append(false, " - invalid", true);
                    append(true, connection.getConnectionStatus().getStatusMessage(), null, "red", false);
                }
                createEmptyRow();

                append(true, connection.getProject().getName(), false);
                append(false, "/", false);
                append(false, connection.getName(), false);

                ConnectionPool connectionPool = connection.getConnectionPool();
                append(true, "Pool size: ", null, null, false);
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
        return Commons.nvl(getPresentableText(), "Object Bundle");
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
        return getObject(objectType, name, (short) 0);
    }

    @Override
    @Nullable
    public DBObject getObject(DBObjectType objectType, String name, short overload) {
        if (objectType == CONSOLE) return this.getConnection().getConsoleBundle().getConsole(name);
        if (objectType == SCHEMA) return getSchema(name);
        if (objectType == USER) return getUser(name);
        if (objectType == ROLE) return getRole(name);
        if (objectType == CHARSET) return getCharset(name);
        if (objectType == SYSTEM_PRIVILEGE) return getSystemPrivilege(name);

        if (objectType.isSchemaObject()) {
            for (DBSchema schema : getPublicSchemas()) {
                DBObject childObject = schema.getChildObject(objectType, name, overload, true);
                if (childObject != null) {
                    return childObject;
                }
            }
        }
        return null;
    }

    private Filter<DBObjectType> getConnectionObjectTypeFilter() {
        return this.getConnection().getSettings().getFilterSettings().getObjectTypeFilterSettings().getTypeFilter();
    }

    @Override
    public void lookupObjectsOfType(Consumer<? super DBObject> consumer, DBObjectType objectType) {
        if (getConnectionObjectTypeFilter().accepts(objectType)) {
            if (objectType == SCHEMA) consumer.acceptAll(getSchemas()); else
            if (objectType == USER) consumer.acceptAll(getUsers()); else
            if (objectType == ROLE) consumer.acceptAll(getRoles()); else
            if (objectType == CHARSET) consumer.acceptAll(getCharsets());
            if (objectType == SYSTEM_PRIVILEGE) consumer.acceptAll(getSystemPrivileges());
        }
    }

    @Override
    public void lookupChildObjectsOfType(Consumer<? super DBObject> consumer, DBObject parentObject, DBObjectType objectType, ObjectTypeFilter filter, DBSchema currentSchema) {
        if (getConnectionObjectTypeFilter().accepts(objectType)) {
            if (parentObject != null && currentSchema != null) {
                if (parentObject instanceof DBSchema) {
                    DBSchema schema = (DBSchema) parentObject;
                    if (objectType.isGeneric()) {
                        Set<DBObjectType> concreteTypes = objectType.getInheritingTypes();
                        for (DBObjectType concreteType : concreteTypes) {
                            if (filter.acceptsObject(schema, currentSchema, concreteType)) {
                                consumer.acceptAll(schema.getChildObjects(concreteType));
                            }
                        }
                    } else {
                        if (filter.acceptsObject(schema, currentSchema, objectType)) {
                            consumer.acceptAll(schema.getChildObjects(objectType));
                        }
                    }

                    boolean synonymsSupported = DatabaseCompatibilityInterface.getInstance(parentObject).supportsObjectType(SYNONYM.getTypeId());
                    if (synonymsSupported && filter.acceptsObject(schema, currentSchema, SYNONYM)) {
                        for (DBSynonym synonym : schema.getSynonyms()) {
                            DBObject underlyingObject = synonym.getUnderlyingObject();
                            if (underlyingObject != null && underlyingObject.isOfType(objectType)) {
                                consumer.accept(synonym);
                            }
                        }
                    }
                } else {
                    if (objectType.isGeneric()) {
                        Set<DBObjectType> concreteTypes = objectType.getInheritingTypes();
                        for (DBObjectType concreteType : concreteTypes) {
                            if (filter.acceptsRootObject(objectType)) {
                                consumer.acceptAll(parentObject.getChildObjects(concreteType));
                            }
                        }
                    } else {
                        if (filter.acceptsRootObject(objectType)) {
                            consumer.acceptAll(parentObject.getChildObjects(objectType));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void refreshObjectsStatus(final @Nullable DBSchemaObject requester) {
        ConnectionHandler connection = this.getConnection();
        if (DatabaseFeature.OBJECT_INVALIDATION.isSupported(connection)) {
            Project project = getProject();
            Progress.background(project, "Updating objects status", true, progress -> {
                try {
                    List<DBSchema> schemas = requester == null ? getSchemas() : requester.getReferencingSchemas();

                    int size = schemas.size();
                    for (int i = 0; i < size; i++) {
                        progress.checkCanceled();
                        if (size > 3) {
                            progress.setIndeterminate(false);
                            progress.setFraction(Progress.progressOf(i, size));
                        }
                        DBSchema schema = schemas.get(i);
                        progress.setText("Updating object status in schema " + schema.getName() + "... ");
                        schema.refreshObjectsStatus();
                    }
                } catch (IndexOutOfBoundsException ignore) {
                    // underlying list may mutate
                } catch (SQLException e) {
                    sendErrorNotification(
                            NotificationGroup.BROWSER,
                            "Error refreshing object status: {0}", e);
                }
            });
        }
    }

    @Override
    @NotNull
    public DBObjectListContainer getObjectLists() {
        return Failsafe.nn(objectLists);
    }

    @Override
    public <T extends DBObject> DBObjectList<T> getObjectList(DBObjectType objectType) {
        return getObjectLists().getObjectList(objectType);
    }

    @Override
    @NotNull
    public Project getProject() {
        return this.getConnection().getProject();
    }

    @Override
    @Nullable
    public DynamicContent<?> getDynamicContent(DynamicContentType<?> dynamicContentType) {
        if(dynamicContentType instanceof DBObjectType) {
            DBObjectType objectType = (DBObjectType) dynamicContentType;
            return Commons.coalesce(
                    () -> objectLists.getObjectList(objectType, false),
                    () -> objectLists.getObjectList(objectType, true));
        }

        if (dynamicContentType instanceof DBObjectRelationType) {
            DBObjectRelationType relationType = (DBObjectRelationType) dynamicContentType;
            return objectLists.getRelations(relationType);
        }

        return null;
    }

    @Override
    public void initTreeElement() {}

    @Override
    public String toString() {
        return this.getConnection().getName();
    }

    /*********************************************************
     *                         Loaders                       *
     *********************************************************/


    static {
        new DynamicContentLoaderImpl<DBConsole, DBObjectMetadata>(null, CONSOLE, true){

            @Override
            public void loadContent(DynamicContent<DBConsole> content, boolean forceReload) {
                ConnectionHandler connection = content.getConnection();
                List<DBConsole> consoles = connection.getConsoleBundle().getConsoles();
                content.setElements(consoles);
            }
        };

        new DynamicContentResultSetLoader<DBSchema, DBSchemaMetadata>(null, SCHEMA, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBSchema> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                return metadataInterface.loadSchemas(connection);
            }

            @Override
            public DBSchema createElement(DynamicContent<DBSchema> content, DBSchemaMetadata metadata, LoaderCache cache) throws SQLException {
                return new DBSchemaImpl(content.getConnection(), metadata);
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
                return new DBUserImpl(content.getConnection(), metadata);
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
                return new DBRoleImpl(content.getConnection(), metadata);
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
                return new DBSystemPrivilegeImpl(content.getConnection(), metadata);
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
                return new DBObjectPrivilegeImpl(content.getConnection(), metadata);
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
                return new DBCharsetImpl(content.getConnection(), metadata);
            }
        };

        new DynamicContentResultSetLoader<DBUserRoleRelation, DBGrantedRoleMetadata>(null, USER_ROLE, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent<DBUserRoleRelation> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                return metadataInterface.loadAllUserRoles(connection);
            }

            @Override
            public DBUserRoleRelation createElement(DynamicContent<DBUserRoleRelation> content, DBGrantedRoleMetadata metadata, LoaderCache cache) throws SQLException {
                String userName = metadata.getUserName();

                DBObjectBundle objectBundle = content.ensureParentEntity();
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
            public ResultSet createResultSet(DynamicContent<DBUserPrivilegeRelation> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                return metadataInterface.loadAllUserPrivileges(connection);
            }

            @Override
            public DBUserPrivilegeRelation createElement(DynamicContent<DBUserPrivilegeRelation> content, DBGrantedPrivilegeMetadata metadata, LoaderCache cache) throws SQLException {
                String userName = metadata.getUserName();

                DBObjectBundle objectBundle = content.ensureParentEntity();
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
            public ResultSet createResultSet(DynamicContent<DBRoleRoleRelation> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                return metadataInterface.loadAllRoleRoles(connection);
            }

            @Override
            public DBRoleRoleRelation createElement(DynamicContent<DBRoleRoleRelation> content, DBGrantedRoleMetadata metadata, LoaderCache cache) throws SQLException {
                String roleName = metadata.getRoleName();

                DBObjectBundle objectBundle = content.ensureParentEntity();
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
            public ResultSet createResultSet(DynamicContent<DBRolePrivilegeRelation> dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                return metadataInterface.loadAllRolePrivileges(connection);
            }

            @Override
            public DBRolePrivilegeRelation createElement(DynamicContent<DBRolePrivilegeRelation> content, DBGrantedPrivilegeMetadata metadata, LoaderCache cache) throws SQLException {
                String userName = metadata.getRoleName();

                DBObjectBundle objectBundle = content.ensureParentEntity();
                DBRole role = objectBundle.getRole(userName);
                if (role != null) {
                    DBGrantedPrivilege privilege = new DBGrantedPrivilegeImpl(role, metadata);
                    return new DBRolePrivilegeRelation(role, privilege);
                }
                return null;
            }
        };
    }


    @Override
    public void disposeInner() {
        SafeDisposer.dispose(objectLists, false);
        SafeDisposer.dispose(dataTypes, false);
        nullify();
    }
}
