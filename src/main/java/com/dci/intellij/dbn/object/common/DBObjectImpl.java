package com.dci.intellij.dbn.object.common;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.browser.model.BrowserTreeEventListener;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.model.BrowserTreeNodeBase;
import com.dci.intellij.dbn.browser.model.LoadInProgressTreeNode;
import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.browser.ui.ToolTipProvider;
import com.dci.intellij.dbn.code.common.lookup.LookupItemBuilder;
import com.dci.intellij.dbn.code.sql.color.SQLTextAttributesKeys;
import com.dci.intellij.dbn.common.consumer.CancellableConsumer;
import com.dci.intellij.dbn.common.consumer.ListCollector;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.routine.Consumer;
import com.dci.intellij.dbn.common.thread.Background;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.util.Commons;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.connection.*;
import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.connection.jdbc.DBNCallableStatement;
import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadata;
import com.dci.intellij.dbn.database.interfaces.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.QuotePair;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBUser;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectListVisitor;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.operation.DBOperationExecutor;
import com.dci.intellij.dbn.object.common.operation.DBOperationNotSupportedException;
import com.dci.intellij.dbn.object.common.property.DBObjectProperties;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.object.properties.ConnectionPresentableProperty;
import com.dci.intellij.dbn.object.properties.DBObjectPresentableProperty;
import com.dci.intellij.dbn.object.properties.PresentableProperty;
import com.dci.intellij.dbn.object.type.DBObjectRelationType;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.dci.intellij.dbn.vfs.file.DBObjectVirtualFile;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiInvalidElementAccessException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

import static com.dci.intellij.dbn.browser.DatabaseBrowserUtils.treeVisibilityChanged;
import static com.dci.intellij.dbn.common.dispose.Failsafe.nd;
import static com.dci.intellij.dbn.common.util.Compactables.compact;
import static com.dci.intellij.dbn.common.util.Lists.filter;
import static com.dci.intellij.dbn.common.util.Unsafe.cast;

public abstract class DBObjectImpl<M extends DBObjectMetadata> extends BrowserTreeNodeBase implements DBObject, ToolTipProvider {

    public static final List<BrowserTreeNode> EMPTY_TREE_NODE_LIST = Collections.unmodifiableList(new ArrayList<>(0));

    private final ConnectionRef connection;
    protected DBObjectRef<?> objectRef;
    protected DBObjectRef<?> parentObjectRef;
    protected DBObjectProperties properties = new DBObjectProperties();

    private volatile List<BrowserTreeNode> allPossibleTreeChildren;
    private volatile List<BrowserTreeNode> visibleTreeChildren;
    private volatile DBObjectListContainer childObjects;

    private static final DBOperationExecutor NULL_OPERATION_EXECUTOR = operationType -> {
        throw new DBOperationNotSupportedException(operationType);
    };

    protected DBObjectImpl(@NotNull DBObject parentObject, M metadata) throws SQLException {
        this.connection = ConnectionRef.of(parentObject.getConnection());
        this.parentObjectRef = DBObjectRef.of(parentObject);
        init(metadata);
    }

    protected DBObjectImpl(@NotNull ConnectionHandler connection, M metadata) throws SQLException {
        this.connection = ConnectionRef.of(connection);
        init(metadata);
    }

    protected DBObjectImpl(@Nullable ConnectionHandler connection, DBObjectType objectType, String name) {
        this.connection = ConnectionRef.of(connection);
        objectRef = new DBObjectRef<>(this, objectType, name);
    }

    private void init(M metadata) throws SQLException {
        String name = initObject(metadata);
        objectRef = new DBObjectRef<>(this, name);

        initStatus(metadata);
        initProperties();
        initLists();
    }

    protected abstract String initObject(M metadata) throws SQLException;

    public void initStatus(M metadata) throws SQLException {}

    protected void initProperties() {}

    protected void initLists() {}

    @Override
    public boolean set(DBObjectProperty status, boolean value) {
        return properties.set(status, value);
    }

    @Override
    public boolean is(DBObjectProperty property) {
        return properties.is(property);
    }

    @Override
    public final DBContentType getContentType() {
        return getObjectType().getContentType();
    }

    @Override
    public DBObjectRef ref() {
        return objectRef;
    }

    @Override
    public boolean isParentOf(DBObject object) {
        return this.equals(object.getParentObject());
    }

    @Override
    public DBOperationExecutor getOperationExecutor() {
        return NULL_OPERATION_EXECUTOR;
    }

    @Override
    public DBSchema getSchema() {
        DBObject object = this;
        while (object != null) {
            if (object instanceof DBSchema) {
                return (DBSchema) object;
            }
            object = object.getParentObject();
        }
        return null;
    }

    public SchemaId getSchemaId() {
        return SchemaId.from(getSchema());
    }

    @Override
    public <T extends DBObject> T getParentObject() {
        return cast(DBObjectRef.get(parentObjectRef));
    }

    @Override
    @Nullable
    public DBObject getDefaultNavigationObject() {
        return null;
    }

    @Override
    public boolean isOfType(DBObjectType objectType) {
        return getObjectType().matches(objectType);
    }

    @Nullable
    @Override
    public <E extends DatabaseEntity> E getParentEntity() {
        return cast(getParentObject());
    }

    @Override
    public String getTypeName() {
        return getObjectType().getName();
    }

    @Override
    @NotNull
    public String getName() {
        return objectRef.getObjectName();
    }

    @Override
    public String getQuotedName(boolean quoteAlways) {
        String name = getName();
        if (quoteAlways || needsNameQuoting()) {
            ConnectionHandler connection = this.getConnection();
            ConnectionDatabaseSettings databaseSettings = connection.getSettings().getDatabaseSettings();
            if (databaseSettings.getDatabaseType() == DatabaseType.GENERIC) {
                String identifierQuotes = connection.getCompatibility().getIdentifierQuote();
                return identifierQuotes + name + identifierQuotes;
            } else {
                DatabaseCompatibilityInterface compatibility = getCompatibilityInterface();
                QuotePair quotes = compatibility.getDefaultIdentifierQuotes();
                return quotes.quote(name);
            }
        } else {
            return name;
        }
    }

    @Override
    public boolean needsNameQuoting() {
        String name = getName();
        return name.indexOf('-') > 0 ||
                name.indexOf('.') > 0 ||
                name.indexOf('#') > 0 ||
                getLanguageDialect(SQLLanguage.INSTANCE).isReservedWord(name) ||
                Strings.isMixedCase(name);
    }

    @Override
    @Nullable
    public Icon getIcon() {
        return getObjectType().getIcon();
    }

    @NotNull
    @Override
    public String getQualifiedName() {
        return objectRef.getPath();
    }

    @Override
    public String getQualifiedNameWithType() {
        return objectRef.getQualifiedNameWithType();
    }

    @Override
    @Nullable
    public DBUser getOwner() {
        DBObject parentObject = getParentObject();
        return parentObject == null ? null : parentObject.getOwner();
    }

    @Override
    public Icon getOriginalIcon() {
        return getIcon();
    }

    @Override
    public String getNavigationTooltipText() {
        DBObject parentObject = getParentObject();
        if (parentObject == null) {
            return getTypeName();
        } else {
            return getTypeName() + " (" +
                    parentObject.getTypeName() + ' ' +
                    parentObject.getName() + ')';
        }
    }


    @Override
    public String getToolTip() {
        if (isDisposed()) {
            return null;
        }
        return new HtmlToolTipBuilder() {
            @Override
            public void buildToolTip() {
                DBObjectImpl.this.buildToolTip(this);
            }
        }.getToolTip();
    }

    public void buildToolTip(HtmlToolTipBuilder ttb) {
        ConnectionHandler connection = this.getConnection();
        ttb.append(true, getQualifiedName(), false);
        ttb.append(true, "Connection: ", null, null, false );
        ttb.append(false, connection.getName(), false);
    }

    @Override
    public DBObjectAttribute[] getObjectAttributes(){return null;}
    @Override
    public DBObjectAttribute getNameAttribute(){return null;}

    @NotNull
    @Override
    public ConnectionId getConnectionId() {
        return connection.getConnectionId();
    }

    @Override
    @NotNull
    public ConnectionHandler getConnection() {
        return ConnectionRef.ensure(connection);
    }

    @NotNull
    @Override
    public EnvironmentType getEnvironmentType() {
        ConnectionHandler connection = this.getConnection();
        return connection.getEnvironmentType();
    }

    @Override
    public DBLanguageDialect getLanguageDialect(DBLanguage language) {
        ConnectionHandler connection = this.getConnection();
        return connection.getLanguageDialect(language);
    }

    @Override
    public DBObjectListContainer getChildObjects() {
        return childObjects;
    }

    public void visitChildObjects(DBObjectListVisitor visitor, boolean visitInternal) {
        if (childObjects != null) childObjects.visit(visitor, visitInternal);
    }

    @Override
    public boolean isEditorReady() {
        if (childObjects == null) return false;
        for (DBObjectList<?> list : childObjects.getObjects()) {
            if (list != null && !list.isInternal() && !list.isLoaded()) return false;
        }
        return true;
    }

    @Override
    public void makeEditorReady() {
        if (childObjects != null) childObjects.loadObjects();
    }

    @NotNull
    public DBObjectListContainer ensureChildObjects() {
        if (childObjects == null) {
            synchronized (this) {
                if (childObjects == null) {
                    childObjects = new DBObjectListContainer(this);
                }
            }
        }
        return childObjects;
    }

    public static DBObject getObjectByName(List<? extends DBObject> objects, String name) {
        if (objects != null) {
            for (DBObject object : objects) {
                if (Objects.equals(object.getName(), name)) {
                    return object;
                }
            }
        }
        return null;
    }

    @Override
    public <T extends DBObject> T  getChildObject(DBObjectType objectType, String name, boolean lookupHidden) {
        return cast(getChildObject(objectType, name, (short) 0, lookupHidden));
    }

    @Override
    public List<String> getChildObjectNames(DBObjectType objectType) {
        if (childObjects != null) {
            DBObjectList objectList = childObjects.getObjectList(objectType);
            if (objectList != null) {
                List<String> objectNames = new ArrayList<>();
                List<DBObject> objects = objectList.getObjects();
                for (DBObject object : objects) {
                    objectNames.add(object.getName());
                }
                return objectNames;
            }
        }
        return java.util.Collections.emptyList();
    }

    @Override
    public <T extends DBObject> T  getChildObject(DBObjectType objectType, String name, short overload, boolean lookupHidden) {
        if (childObjects == null) {
            return null;
        } else {
            DBObject object = childObjects.getObject(objectType, name, overload);
            if (object == null && lookupHidden) {
                object = childObjects.getInternalObject(objectType, name, overload);
            }
            return cast(object);
        }
    }

    @Override
    @Nullable
    public DBObject getChildObject(String name, boolean lookupHidden) {
        return getChildObject(name, (short) 0, lookupHidden);
    }

    @Override
    @Nullable
    public DBObject getChildObject(String name, short overload, boolean lookupHidden) {
        return childObjects == null ? null :
                childObjects.getObjectForParentType(this.getObjectType(), name, overload, lookupHidden);
    }

    public DBObject getChildObjectNoLoad(String name) {
        return getChildObjectNoLoad(name, (short) 0);
    }

    public DBObject getChildObjectNoLoad(String name, short overload) {
        return childObjects == null ? null : childObjects.getObjectNoLoad(name, overload);
    }

    @Override
    @NotNull
    public List<DBObject> getChildObjects(DBObjectType objectType) {
        ListCollector<DBObject> collector = ListCollector.basic();
        collectChildObjects(objectType, collector);
        return collector.elements();
    }

    @Override
    public void collectChildObjects(DBObjectType objectType, Consumer<? super DBObject> consumer) {
        if (childObjects == null) return;

        Set<DBObjectType> familyTypes = objectType.getFamilyTypes();
        if (familyTypes.size() > 1) {
            for (DBObjectType familyType : familyTypes) {
                CancellableConsumer.checkCancelled(consumer);
                if (objectType != familyType) {
                    if (getObjectType().isParentOf(familyType)) {
                        collectChildObjects(familyType, consumer);
                    }
                } else {
                    DBObjectList<?> objectList = childObjects.getObjectList(objectType);
                    if (objectList != null) {
                        objectList.collectObjects(consumer);
                    }
                }
            }
        } else {
            if (objectType == DBObjectType.ANY) {
                childObjects.visit(o -> o.collectObjects(consumer), false);
            } else {
                DBObjectList<?> objectList = Commons.coalesce(
                        () -> childObjects.getObjectList(objectType, false),
                        () -> childObjects.getObjectList(objectType, true));

                if (objectList != null) objectList.collectObjects(consumer);
            }
        }
    }



    @Nullable
    @Override
    public DBObjectList<? extends DBObject> getChildObjectList(DBObjectType objectType) {
        return childObjects == null ? null : childObjects.getObjectList(objectType);
    }

    @Override
    public List<DBObjectNavigationList> getNavigationLists() {
        // todo consider caching;
        return createNavigationLists();
    }

    @Nullable
    protected List<DBObjectNavigationList> createNavigationLists() {
        return null;
    }

    @Override
    @NotNull
    public LookupItemBuilder getLookupItemBuilder(DBLanguage language) {
        return getObjectBundle().getLookupItemBuilder(objectRef, language);
    }

    @Override
    @NotNull
    public DBObjectPsiCache getPsiCache() {
        return getObjectBundle().getObjectPsiCache(ref());
    }

    @Override
    @NotNull
    public DBObjectVirtualFile<?> getVirtualFile() {
        return getObjectBundle().getObjectVirtualFile(ref());
    }

    @Override
    public String extractDDL() throws SQLException {
        // TODO move to database interface (ORACLE)
        ConnectionHandler connection = getConnection();
        return PooledConnection.call(connection.createConnectionContext(), conn -> {
            DBNCallableStatement statement = null;
            try {
                DBObjectType objectType = getObjectType();
                DBObjectType genericType = objectType.getGenericType();
                objectType = genericType == DBObjectType.TRIGGER ? genericType : objectType;
                String objectTypeName = objectType.getName().toUpperCase();

                statement = conn.prepareCall("{? = call DBMS_METADATA.GET_DDL(?, ?, ?)}");
                statement.registerOutParameter(1, Types.CLOB);
                statement.setString(2, objectTypeName);
                statement.setString(3, getName());
                statement.setString(4, getSchema().getName());

                statement.execute();
                String ddl = statement.getString(1);
                return ddl == null ? null : ddl.trim();
            } finally {
                Resources.close(statement);
            }
        });
    }

    @Override
    @Nullable
    public DBObject getUndisposedEntity() {
        return objectRef.get();
    }

    @Override
    @Nullable
    public DynamicContent getDynamicContent(DynamicContentType dynamicContentType) {
        if (childObjects != null) {
            if(dynamicContentType instanceof DBObjectType) {
                DBObjectType objectType = (DBObjectType) dynamicContentType;
                return Commons.coalesce(
                        () -> childObjects.getObjectList(objectType, false),
                        () -> childObjects.getObjectList(objectType, true));
            }

            else if (dynamicContentType instanceof DBObjectRelationType) {
                DBObjectRelationType objectRelationType = (DBObjectRelationType) dynamicContentType;
                return childObjects.getRelations(objectRelationType);
            }
        }

        return null;
    }

    @Override
    public final void reload() {
        if (childObjects != null) {
            childObjects.reloadObjects();
        }
    }

    @Override
    public final void refresh() {
        if (childObjects != null) {
            childObjects.refreshObjects();
        }
    }

    public final void refresh(@NotNull DBObjectType childObjectType) {
        DBObjectList objectList = getChildObjectList(childObjectType);
        if (objectList != null) {
            objectList.refresh();
        }
    }

    /*********************************************************
     *                   NavigationItem                      *
     *********************************************************/
    public FileStatus getFileStatus() {
        return FileStatus.UNKNOWN;
    }

    @Override
    public ItemPresentation getPresentation() {
        return this;
    }

    public TextAttributesKey getTextAttributesKey() {
        return SQLTextAttributesKeys.IDENTIFIER;
    }

    @Override
    public String getLocationString() {
        return null;
    }

    @Override
    public Icon getIcon(boolean open) {
        return getIcon();
    }

    /*********************************************************
     *                  BrowserTreeNode                   *
     *********************************************************/
    @Override
    public void initTreeElement() {}

    @Override
    public boolean isTreeStructureLoaded() {
        return properties.is(DBObjectProperty.TREE_LOADED);
    }

    @Override
    public boolean canExpand() {
        return !isLeaf() && isTreeStructureLoaded() && getChildAt(0).isTreeStructureLoaded();
    }

    @Override
    public Icon getIcon(int flags) {
        return getIcon();
    }

    @Override
    public String getPresentableText() {
        return getName();
    }

    @Override
    public String getPresentableTextDetails() {
        return null;
    }

    @Override
    public String getPresentableTextConditionalDetails() {
        return null;
    }

    @Override
    @NotNull
    public BrowserTreeNode getParent() {
        DBObjectType objectType = getObjectType();
        if (parentObjectRef != null){
            DBObject object = parentObjectRef.ensure();

            DBObjectListContainer childObjects = nd(object.getChildObjects());
            DBObjectList parentObjectList = childObjects.getObjectList(objectType);
            return nd(parentObjectList);

        } else {
            DBObjectList<?> parentObjectList = getObjectBundle().getObjectList(objectType);
            return nd(parentObjectList);
        }
    }



    @Override
    public int getTreeDepth() {
        BrowserTreeNode treeParent = getParent();
        return treeParent.getTreeDepth() + 1;
    }


    @NotNull
    public List<BrowserTreeNode> getAllPossibleTreeChildren() {
        if (allPossibleTreeChildren == null) {
            synchronized (this) {
                if (allPossibleTreeChildren == null) {
                    allPossibleTreeChildren = buildAllPossibleTreeChildren();
                    allPossibleTreeChildren = compact(allPossibleTreeChildren);
                }
            }
        }
        return allPossibleTreeChildren;
    }



    @Override
    public List<? extends BrowserTreeNode> getChildren() {
        if (visibleTreeChildren == null) {
            synchronized (this) {
                if (visibleTreeChildren == null) {
                    visibleTreeChildren = new ArrayList<>();
                    visibleTreeChildren.add(new LoadInProgressTreeNode(this));

                    Background.run(getProject(), () -> buildTreeChildren());
                }
            }
        }
        return visibleTreeChildren;
    }

    private void buildTreeChildren() {
        checkDisposed();
        ConnectionHandler connection = this.getConnection();
        Filter<BrowserTreeNode> objectTypeFilter = connection.getObjectTypeFilter();

        List<BrowserTreeNode> treeChildren = filter(getAllPossibleTreeChildren(), objectTypeFilter);
        treeChildren = Commons.nvl(treeChildren, Collections.emptyList());

        for (BrowserTreeNode objectList : treeChildren) {
            Background.run(getProject(), () -> objectList.initTreeElement());
            checkDisposed();
        }

        if (visibleTreeChildren.size() == 1 && visibleTreeChildren.get(0) instanceof LoadInProgressTreeNode) {
            visibleTreeChildren.get(0).dispose();
        }

        visibleTreeChildren = treeChildren;
        visibleTreeChildren = compact(visibleTreeChildren);
        set(DBObjectProperty.TREE_LOADED, true);


        Project project = Failsafe.nn(getProject());
        ProjectEvents.notify(project,
                BrowserTreeEventListener.TOPIC,
                (listener) -> listener.nodeChanged(this, TreeEventType.STRUCTURE_CHANGED));
        DatabaseBrowserManager.scrollToSelectedElement(this.getConnection());
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
            ConnectionHandler connection = this.getConnection();
            Filter<BrowserTreeNode> filter = connection.getObjectTypeFilter();

            if (treeVisibilityChanged(getAllPossibleTreeChildren(), visibleTreeChildren, filter)) {
                buildTreeChildren();
            }
            for (BrowserTreeNode treeNode : visibleTreeChildren) {
                treeNode.rebuildTreeChildren();
            }
        }


    }

    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return EMPTY_TREE_NODE_LIST;
    }

    @Override
    public boolean isLeaf() {
        return Failsafe.guarded(true, () -> {
            if (visibleTreeChildren == null) {
                ConnectionHandler connection = this.getConnection();
                Filter<BrowserTreeNode> filter = connection.getObjectTypeFilter();
                for (BrowserTreeNode treeNode : getAllPossibleTreeChildren() ) {
                    if (treeNode != null && filter.accepts(treeNode)) {
                        return false;
                    }
                }
                return true;
            } else {
                return visibleTreeChildren.size() == 0;
            }
        });
    }

    @Override
    public BrowserTreeNode getChildAt(int index) {
        return getChildren().get(index);
    }

    @Override
    public int getChildCount() {
        return getChildren().size();
    }

    @Override
    public int getIndex(BrowserTreeNode child) {
        return getChildren().indexOf(child);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof DBObject) {
            DBObject object = (DBObject) obj;
            return objectRef.equals(object.ref());
        }
        return false;
    }


    public int hashCode() {
        return objectRef.hashCode();
    }

    @Override
    @NotNull
    public Project getProject() throws PsiInvalidElementAccessException {
        ConnectionHandler connection = Failsafe.nn(this.getConnection());
        return connection.getProject();
    }

    @Override
    public int compareTo(@NotNull Object o) {
        if (o instanceof DBObject) {
            DBObject object = (DBObject) o;
            return objectRef.compareTo(object.ref());
        }
        return -1;
    }

    public String toString() {
        return getName();
    }

    @Override
    public List<PresentableProperty> getPresentableProperties() {
        List<PresentableProperty> properties = new ArrayList<>();
        DBObject parent = getParentObject();
        while (parent != null) {
            properties.add(new DBObjectPresentableProperty(parent));
            parent = parent.getParentObject();
        }
        properties.add(new ConnectionPresentableProperty(this.getConnection()));

        return properties;
    }

    @Override
    public boolean isValid() {
        return !isDisposed();
    }

    @Override
    public boolean isVirtual() {
        return false;
    }

    /*********************************************************
    *               DynamicContentElement                    *
    *********************************************************/

    @Override
    public String getDescription() {
        return getQualifiedName();
    }

    /*********************************************************
    *                      Navigatable                      *
    *********************************************************/
    @Override
    public void navigate(boolean requestFocus) {
        DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(getProject());
        browserManager.navigateToElement(this, requestFocus, true);
    }

    @Override
    public boolean canNavigate() {
        return true;
    }

    /*********************************************************
     *                   PsiElement                          *
     *********************************************************/

    //@Override
    public PsiFile getContainingFile() throws PsiInvalidElementAccessException {
        return DBObjectPsiCache.asPsiFile(this);
    }

    @Override
    public boolean canNavigateToSource() {
        return false;
    }

    @Override
    public void disposeInner() {
        Disposer.dispose(childObjects, false);
        nullify();
    }
}
