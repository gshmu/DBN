package com.dci.intellij.dbn.object.common;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.browser.model.BrowserTreeEventListener;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.model.BrowserTreeNodeBase;
import com.dci.intellij.dbn.browser.model.LoadInProgressTreeNode;
import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.browser.ui.ToolTipProvider;
import com.dci.intellij.dbn.code.common.lookup.LookupItemBuilder;
import com.dci.intellij.dbn.code.common.lookup.ObjectLookupItemBuilder;
import com.dci.intellij.dbn.code.sql.color.SQLTextAttributesKeys;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.thread.SimpleBackgroundInvocator;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.connection.jdbc.DBNCallableStatement;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.QuotePair;
import com.dci.intellij.dbn.language.psql.PSQLLanguage;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBUser;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.list.DBObjectRelationListContainer;
import com.dci.intellij.dbn.object.common.operation.DBOperationExecutor;
import com.dci.intellij.dbn.object.common.operation.DBOperationNotSupportedException;
import com.dci.intellij.dbn.object.common.property.DBObjectProperties;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.object.properties.ConnectionPresentableProperty;
import com.dci.intellij.dbn.object.properties.DBObjectPresentableProperty;
import com.dci.intellij.dbn.object.properties.PresentableProperty;
import com.dci.intellij.dbn.vfs.file.DBObjectVirtualFile;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiInvalidElementAccessException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class DBObjectImpl extends BrowserTreeNodeBase implements DBObject, ToolTipProvider {
    private static final List<DBObject> EMPTY_OBJECT_LIST = Collections.unmodifiableList(new ArrayList<>(0));
    public static final List<BrowserTreeNode> EMPTY_TREE_NODE_LIST = Collections.unmodifiableList(new ArrayList<BrowserTreeNode>(0));

    private List<BrowserTreeNode> allPossibleTreeChildren;
    private List<BrowserTreeNode> visibleTreeChildren;

    protected String name;
    protected DBObjectRef objectRef;
    protected DBObjectRef parentObjectRef;

    protected DBObjectPsiFacade psiFacade;

    protected DBObjectProperties properties = new DBObjectProperties();
    private DBObjectListContainer childObjects;
    private DBObjectRelationListContainer childObjectRelations;


    private LookupItemBuilder sqlLookupItemBuilder;
    private LookupItemBuilder psqlLookupItemBuilder;
    private ConnectionHandlerRef connectionHandlerRef;

    protected DBObjectVirtualFile virtualFile;

    private static final DBOperationExecutor NULL_OPERATION_EXECUTOR = operationType -> {
        throw new DBOperationNotSupportedException(operationType);
    };

    protected DBObjectImpl(@NotNull DBObject parentObject, ResultSet resultSet) throws SQLException {
        this.connectionHandlerRef = ConnectionHandlerRef.from(parentObject.getConnectionHandler());
        this.parentObjectRef = DBObjectRef.from(parentObject);
        init(resultSet);
    }

    protected DBObjectImpl(@NotNull ConnectionHandler connectionHandler, ResultSet resultSet) throws SQLException {
        this.connectionHandlerRef = ConnectionHandlerRef.from(connectionHandler);
        init(resultSet);
    }

    protected DBObjectImpl(@Nullable ConnectionHandler connectionHandler, String name) {
        this.connectionHandlerRef = ConnectionHandlerRef.from(connectionHandler);
        this.name = name;
    }

    private void init(ResultSet resultSet) throws SQLException {
        initObject(resultSet);
        initStatus(resultSet);
        initProperties();
        initLists();

        CollectionUtil.compact(childObjects);
        CollectionUtil.compact(childObjectRelations);
        objectRef = new DBObjectRef(this);
    }

    protected abstract void initObject(ResultSet resultSet) throws SQLException;

    public void initStatus(ResultSet resultSet) throws SQLException {}

    protected void initProperties() {}

    protected void initLists() {}

/*    @Override
    public PsiElement getParent() {
        PsiFile containingFile = getContainingFile();
        if (containingFile != null) {
            return containingFile.getParent();
        }
        return null;
    }*/

    @Override
    public boolean set(DBObjectProperty status, boolean value) {
        return properties.set(status, value);
    }

    @Override
    public boolean is(DBObjectProperty property) {
        return properties.is(property);
    }

    public DBContentType getContentType() {
        return DBContentType.NONE;
    }

    @Override
    public DBObjectRef getRef() {
        return objectRef;
    }


    @Override
    public DBObjectPsiFacade getPsiFacade() {
        if (psiFacade == null) {
            synchronized (this) {
                if (psiFacade == null) {
                    FailsafeUtil.check(this);
                    psiFacade = new DBObjectPsiFacade(objectRef);
                }
            }
        }
        return psiFacade;
    }

    @Override
    public boolean isParentOf(DBObject object) {
        return this.equals(object.getParentObject());
    }

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

    public DBObject getParentObject() {
        return DBObjectRef.get(parentObjectRef);
    }

    @Nullable
    public DBObject getDefaultNavigationObject() {
        return null;
    }

    public boolean isOfType(DBObjectType objectType) {
        return getObjectType().matches(objectType);
    }

    @Nullable
    @Override
    public GenericDatabaseElement getParentElement() {
        return getParentObject();
    }

    public String getTypeName() {
        return getObjectType().getName();
    }

    @NotNull
    public String getName() {
        return name;
    }

    @Override
    public int getOverload() {
        return 0;
    }

    @Override
    public String getQuotedName(boolean quoteAlways) {
        if (quoteAlways || needsNameQuoting()) {
            DatabaseCompatibilityInterface compatibilityInterface = DatabaseCompatibilityInterface.getInstance(this);
            QuotePair quotes = compatibilityInterface.getDefaultIdentifierQuotes();
            return quotes.beginChar() + name + quotes.endChar();
        } else {
            return name;
        }
    }

    public boolean needsNameQuoting() {
        return name.indexOf('-') > 0 ||
                name.indexOf('.') > 0 ||
                name.indexOf('#') > 0 ||
                getLanguageDialect(SQLLanguage.INSTANCE).isReservedWord(name) ||
                StringUtil.isMixedCase(name);
    }

    @Nullable
    public Icon getIcon() {
        return getObjectType().getIcon();
    }

    public String getQualifiedName() {
        return objectRef.getPath();
    }

    @Override
    public String getQualifiedNameWithType() {
        return objectRef.getQualifiedNameWithType();
    }

    @Nullable
    public DBUser getOwner() {
        DBObject parentObject = getParentObject();
        return parentObject == null ? null : parentObject.getOwner();
    }

    public Icon getOriginalIcon() {
        return getIcon();
    }

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


    public String getToolTip() {
        if (isDisposed()) {
            return null;
        }
        return new HtmlToolTipBuilder() {
            public void buildToolTip() {
                DBObjectImpl.this.buildToolTip(this);
            }
        }.getToolTip();
    }

    public void buildToolTip(HtmlToolTipBuilder ttb) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        ttb.append(true, getQualifiedName(), false);
        ttb.append(true, "Connection: ", "-2", null, false );
        ttb.append(false, connectionHandler.getPresentableText(), false);
    }

    public DBObjectAttribute[] getObjectAttributes(){return null;}
    public DBObjectAttribute getNameAttribute(){return null;}

    @NotNull
    @Override
    public DBObjectBundle getObjectBundle() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        return connectionHandler.getObjectBundle();
    }

    @NotNull
    public ConnectionHandler getConnectionHandler() {
        return ConnectionHandlerRef.getnn(connectionHandlerRef);
    }

    @NotNull
    @Override
    public EnvironmentType getEnvironmentType() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        return connectionHandler.getEnvironmentType();
    }

    public DBLanguageDialect getLanguageDialect(DBLanguage language) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        return connectionHandler.getLanguageDialect(language);
    }

    public DBObjectListContainer getChildObjects() {
        return childObjects;
    }

    public DBObjectRelationListContainer getChildObjectRelations() {
        return childObjectRelations;
    }

    @NotNull
    public DBObjectListContainer initChildObjects() {
        if (childObjects == null) {
            synchronized (this) {
                if (childObjects == null) {
                    childObjects = new DBObjectListContainer(this);
                }
            }
        }
        return childObjects;
    }

    @NotNull
    public DBObjectRelationListContainer initChildObjectRelations() {
        if (childObjectRelations == null) {
            synchronized (this) {
                if (childObjectRelations == null) {
                    childObjectRelations = new DBObjectRelationListContainer(this);
                }
            }
        }
        return childObjectRelations;

    }

    public static DBObject getObjectByName(List<? extends DBObject> objects, String name) {
        if (objects != null) {
            for (DBObject object : objects) {
                if (object.getName().equals(name)) {
                    return object;
                }
            }
        }
        return null;
    }

    public DBObject getChildObject(DBObjectType objectType, String name, boolean lookupHidden) {
        return getChildObject(objectType, name, 0, lookupHidden);
    }

    public DBObject getChildObject(DBObjectType objectType, String name, int overload, boolean lookupHidden) {
        if (childObjects == null) {
            return null;
        } else {
            DBObject object = childObjects.getObject(objectType, name, overload);
            if (object == null && lookupHidden) {
                object = childObjects.getInternalObject(objectType, name, overload);
            }
            return object;
        }
    }

    @Nullable
    public DBObject getChildObject(String name, boolean lookupHidden) {
        return getChildObject(name, 0, lookupHidden);
    }

    @Nullable
    public DBObject getChildObject(String name, int overload, boolean lookupHidden) {
        return childObjects == null ? null :
                childObjects.getObjectForParentType(this.getObjectType(), name, overload, lookupHidden);
    }

    public DBObject getChildObjectNoLoad(String name) {
        return getChildObjectNoLoad(name, 0);
    }

    public DBObject getChildObjectNoLoad(String name, int overload) {
        return childObjects == null ? null : childObjects.getObjectNoLoad(name, overload);
    }

    @NotNull
    public List<DBObject> getChildObjects(DBObjectType objectType) {
        if (objectType.getFamilyTypes().size() > 1) {
            List<DBObject> list = new ArrayList<DBObject>();
            for (DBObjectType childObjectType : objectType.getFamilyTypes()) {
                if (objectType != childObjectType) {
                    List<DBObject> childObjects = getChildObjects(childObjectType);
                    list.addAll(childObjects);
                } else {
                    DBObjectList<? extends DBObject> objectList = childObjects == null ? null : childObjects.getObjectList(objectType);
                    if (objectList != null) {
                        list.addAll(objectList.getObjects());
                    }
                }
            }
            return list;
        } else {
            if (objectType == DBObjectType.ANY) {
                Collection<DBObjectList<DBObject>> objectLists = childObjects.getObjectLists();
                if (objectLists != null) {
                    List<DBObject> objects = new ArrayList<DBObject>();
                    for (DBObjectList objectList : objectLists) {
                        if (FailsafeUtil.softCheck(objectList) && !objectList.isInternal())
                        objects.addAll(objectList.getObjects());
                    }
                    return objects;
                }
                return EMPTY_OBJECT_LIST;
            } else {
                DBObjectList objectList = null;
                if (childObjects != null) {
                    objectList = childObjects.getObjectList(objectType);
                    if (objectList == null) {
                        objectList = childObjects.getInternalObjectList(objectType);
                    }
                }
                return objectList == null ? EMPTY_OBJECT_LIST : objectList.getObjects();
            }
        }
    }

    @Nullable
    @Override
    public DBObjectList<? extends DBObject> getChildObjectList(DBObjectType objectType) {
        return childObjects == null ? null : childObjects.getObjectList(objectType);
    }

    public List<DBObjectNavigationList> getNavigationLists() {
        // todo consider caching;
        return createNavigationLists();
    }

    protected List<DBObjectNavigationList> createNavigationLists() {
        return null;
    }

    public LookupItemBuilder getLookupItemBuilder(DBLanguage language) {
        if (language == SQLLanguage.INSTANCE) {
            if (sqlLookupItemBuilder == null) {
                sqlLookupItemBuilder = new ObjectLookupItemBuilder(this, language);
            }
            return sqlLookupItemBuilder;
        }
        if (language == PSQLLanguage.INSTANCE) {
            if (psqlLookupItemBuilder == null) {
                psqlLookupItemBuilder = new ObjectLookupItemBuilder(this, language);
            }
            return psqlLookupItemBuilder;
        }
        return null;
    }

    public String extractDDL() throws SQLException {
        String ddl;
        DBNCallableStatement statement = null;
        DBNConnection connection = null;

        ConnectionHandler connectionHandler = FailsafeUtil.get(getConnectionHandler());
        try {
            connection = connectionHandler.getPoolConnection(true);
            statement = connection.prepareCall("{? = call DBMS_METADATA.GET_DDL(?, ?, ?)}");
            statement.registerOutParameter(1, Types.CLOB);
            statement.setString(2, getTypeName().toUpperCase());
            statement.setString(3, name);
            statement.setString(4, getSchema().getName());

            statement.execute();
            ddl = statement.getString(1);
            ddl = ddl == null ? null : ddl.trim();
        } finally{
            ConnectionUtil.close(statement);
            connectionHandler.freePoolConnection(connection);
        }
        return ddl;
    }

    @Nullable
    public DBObject getUndisposedElement() {
        return objectRef.get();
    }

    @Nullable
    public DynamicContent getDynamicContent(DynamicContentType dynamicContentType) {
        if(dynamicContentType instanceof DBObjectType && childObjects != null) {
            DBObjectType objectType = (DBObjectType) dynamicContentType;
            DynamicContent dynamicContent = childObjects.getObjectList(objectType);
            if (dynamicContent == null) dynamicContent = childObjects.getInternalObjectList(objectType);
            return dynamicContent;
        }

        else if (dynamicContentType instanceof DBObjectRelationType && childObjectRelations != null) {
            DBObjectRelationType objectRelationType = (DBObjectRelationType) dynamicContentType;
            return childObjectRelations.getObjectRelationList(objectRelationType);
        }

        return null;
    }

    public final void reload() {
        if (childObjects != null) {
            childObjects.reload();
        }
    }

    public final void refresh() {
        if (childObjects != null) {
            childObjects.refresh();
        }
    }

    @NotNull
    public DBObjectVirtualFile getVirtualFile() {
        if (virtualFile == null) {
            synchronized (this) {
                if (virtualFile == null) {
                    virtualFile = new DBObjectVirtualFile(this);
                    Disposer.register(this, virtualFile);
                }
            }
        }
        return virtualFile;
    }

    /*********************************************************
     *                   NavigationItem                      *
     *********************************************************/
    public FileStatus getFileStatus() {
        return FileStatus.UNKNOWN;
    }

    public ItemPresentation getPresentation() {
        return this;
    }

    public TextAttributesKey getTextAttributesKey() {
        return SQLTextAttributesKeys.IDENTIFIER;
    }

    public String getLocationString() {
        return null;
    }

    public Icon getIcon(boolean open) {
        return getIcon();
    }

    /*********************************************************
     *                  BrowserTreeNode                   *
     *********************************************************/
    public void initTreeElement() {}

    public boolean isTreeStructureLoaded() {
        return properties.is(DBObjectProperty.TREE_LOADED);
    }

    public boolean canExpand() {
        return !isLeaf() && isTreeStructureLoaded() && getChildAt(0).isTreeStructureLoaded();
    }

    public Icon getIcon(int flags) {
        return getIcon();
    }

    public String getPresentableText() {
        return name;
    }

    public String getPresentableTextDetails() {
        return null;
    }

    public String getPresentableTextConditionalDetails() {
        return null;
    }

    @NotNull
    public BrowserTreeNode getParent() {
        DBObjectType objectType = getObjectType();
        if (parentObjectRef != null){
            DBObject object = parentObjectRef.get();
            if (object != null) {
                DBObjectListContainer childObjects = object.getChildObjects();
                if (childObjects != null) {
                    DBObjectList parentObjectList = childObjects.getObjectList(objectType);
                    return FailsafeUtil.get(parentObjectList);
                }
            }
        } else {
            DBObjectBundle objectBundle = getObjectBundle();
            DBObjectListContainer objectListContainer = objectBundle.getObjectListContainer();
            DBObjectList parentObjectList = objectListContainer.getObjectList(objectType);
            return FailsafeUtil.get(parentObjectList);
        }
        throw AlreadyDisposedException.INSTANCE;
    }



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
                    CollectionUtil.compact(allPossibleTreeChildren);
                }
            }
        }
        return allPossibleTreeChildren;
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
        ConnectionHandler connectionHandler = FailsafeUtil.get(getConnectionHandler());

        Filter<BrowserTreeNode> filter = connectionHandler.getObjectTypeFilter();
        List<BrowserTreeNode> allPossibleTreeChildren = getAllPossibleTreeChildren();
        List<BrowserTreeNode> newTreeChildren = allPossibleTreeChildren;
        if (allPossibleTreeChildren.size() > 0) {
            if (!filter.acceptsAll(allPossibleTreeChildren)) {
                newTreeChildren = new ArrayList<>();
                for (BrowserTreeNode treeNode : allPossibleTreeChildren) {
                    if (treeNode != null && filter.accepts(treeNode)) {
                        DBObjectList objectList = (DBObjectList) treeNode;
                        newTreeChildren.add(objectList);
                    }
                }
            }
            newTreeChildren = new ArrayList<>(newTreeChildren);

            for (BrowserTreeNode treeNode : newTreeChildren) {
                DBObjectList objectList = (DBObjectList) treeNode;
                objectList.initTreeElement();
                FailsafeUtil.check(this);
            }

            if (visibleTreeChildren.size() == 1 && visibleTreeChildren.get(0) instanceof LoadInProgressTreeNode) {
                visibleTreeChildren.get(0).dispose();
            }
        }
        visibleTreeChildren = newTreeChildren;
        CollectionUtil.compact(visibleTreeChildren);
        set(DBObjectProperty.TREE_LOADED, true);


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
        ConnectionHandler connectionHandler = getConnectionHandler();
        Filter<BrowserTreeNode> filter = connectionHandler.getObjectTypeFilter();
        if (visibleTreeChildren != null && DatabaseBrowserUtils.treeVisibilityChanged(getAllPossibleTreeChildren(), visibleTreeChildren, filter)) {
            buildTreeChildren();
        }
        if (visibleTreeChildren != null) {
            for (BrowserTreeNode treeNode : visibleTreeChildren) {
                treeNode.rebuildTreeChildren();
            }
        }
    }

    @NotNull
    public abstract List<BrowserTreeNode> buildAllPossibleTreeChildren();

    public boolean isLeaf() {
        if (visibleTreeChildren == null) {
            ConnectionHandler connectionHandler = getConnectionHandler();
            Filter<BrowserTreeNode> filter = connectionHandler.getObjectTypeFilter();
            for (BrowserTreeNode treeNode : getAllPossibleTreeChildren() ) {
                if (treeNode != null && filter.accepts(treeNode)) {
                    return false;
                }
            }
            return true;
        } else {
            return visibleTreeChildren.size() == 0;
        }
    }

    public BrowserTreeNode getChildAt(int index) {
        return getChildren().get(index);
    }

    public int getChildCount() {
        return getChildren().size();
    }

    public int getIndex(BrowserTreeNode child) {
        return getChildren().indexOf(child);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof DBObject) {
            DBObject object = (DBObject) obj;
            return objectRef.equals(object.getRef());
        }
        return false;
    }


    public int hashCode() {
        return objectRef.hashCode();
    }

    @NotNull
    public Project getProject() throws PsiInvalidElementAccessException {
        ConnectionHandler connectionHandler = FailsafeUtil.get(getConnectionHandler());
        return connectionHandler.getProject();
    }

    public int compareTo(@NotNull Object o) {
        if (o instanceof DBObject) {
            DBObject object = (DBObject) o;
            return objectRef.compareTo(object.getRef());
        }
        return -1;
    }

    public String toString() {
        return name;
    }

    public List<PresentableProperty> getPresentableProperties() {
        List<PresentableProperty> properties = new ArrayList<>();
        DBObject parent = getParentObject();
        while (parent != null) {
            properties.add(new DBObjectPresentableProperty(parent));
            parent = parent.getParentObject();
        }
        properties.add(new ConnectionPresentableProperty(getConnectionHandler()));

        return properties;
    }

    public boolean isValid() {
        return !isDisposed();
    }

    /*********************************************************
    *               DynamicContentElement                    *
    *********************************************************/
    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
            psiFacade = null;
            DisposerUtil.dispose(childObjects);
            DisposerUtil.dispose(childObjectRelations);
            CollectionUtil.clearCollection(visibleTreeChildren);
            CollectionUtil.clearCollection(allPossibleTreeChildren);
        }
    }

    public String getDescription() {
        return getQualifiedName();
    }

    /*********************************************************
    *                      Navigatable                      *
    *********************************************************/
    public void navigate(boolean requestFocus) {
        DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(getProject());
        browserManager.navigateToElement(this, requestFocus, true);
    }

    public boolean canNavigate() {
        return true;
    }

    /*********************************************************
     *                   PsiElement                          *
     *********************************************************/

    //@Override
    public PsiFile getContainingFile() throws PsiInvalidElementAccessException {
        return DBObjectPsiFacade.getPsiFile(this);
    }

    @Override
    public boolean canNavigateToSource() {
        return false;
    }
}
