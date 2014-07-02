package com.dci.intellij.dbn.object.common;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.browser.model.BrowserTreeChangeListener;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.model.LoadInProgressTreeNode;
import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.browser.ui.ToolTipProvider;
import com.dci.intellij.dbn.code.common.lookup.DBObjectLookupItemFactory;
import com.dci.intellij.dbn.code.common.lookup.LookupItemFactory;
import com.dci.intellij.dbn.code.sql.color.SQLTextAttributesKeys;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.thread.SimpleBackgroundTask;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.psql.PSQLLanguage;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.dci.intellij.dbn.navigation.psi.NavigationPsiCache;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBUser;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.list.DBObjectRelationListContainer;
import com.dci.intellij.dbn.object.common.operation.DBOperationExecutor;
import com.dci.intellij.dbn.object.common.operation.DBOperationNotSupportedException;
import com.dci.intellij.dbn.object.common.operation.DBOperationType;
import com.dci.intellij.dbn.object.common.property.DBObjectProperties;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.object.properties.ConnectionPresentableProperty;
import com.dci.intellij.dbn.object.properties.DBObjectPresentableProperty;
import com.dci.intellij.dbn.object.properties.PresentableProperty;
import com.dci.intellij.dbn.vfs.DatabaseObjectFile;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiInvalidElementAccessException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public abstract class DBObjectImpl extends DBObjectPsiAbstraction implements DBObject, ToolTipProvider {
    private DBContentType contentType = DBContentType.NONE;
    private List<BrowserTreeNode> allPossibleTreeChildren;
    private List<BrowserTreeNode> visibleTreeChildren;
    private boolean treeChildrenLoaded;
    private boolean isDisposed = false;

    protected String name;
    protected DBObjectRef objectRef;
    private DBObjectProperties properties;
    private DBObjectListContainer childObjects;
    private DBObjectRelationListContainer childObjectRelations;
    private DBObjectRef parentObject;
    private DBObjectBundle objectBundle;

    private LookupItemFactory sqlLookupItemFactory;
    private LookupItemFactory psqlLookupItemFactory;

    protected DatabaseObjectFile virtualFile;

    private static final DBOperationExecutor NULL_OPERATION_EXECUTOR = new DBOperationExecutor() {
        public void executeOperation(DBOperationType operationType) throws SQLException, DBOperationNotSupportedException {
            throw new DBOperationNotSupportedException(operationType);
        }
    };

    protected Logger getLogger() {return Logger.getInstance(getClass().getName());}

    public DBObjectImpl(DBObject parentObject, DBContentType contentType, ResultSet resultSet) throws SQLException {
        this.parentObject = parentObject.getRef();
        this.contentType = contentType;
        init(resultSet);
    }

    public DBObjectImpl(DBObjectBundle objectBundle, DBContentType contentType, ResultSet resultSet) throws SQLException {
        this.objectBundle = objectBundle;
        this.contentType = contentType;
        init(resultSet);
    }

    public DBObjectImpl(DBObjectBundle objectBundle, String name) {
        this.objectBundle = objectBundle;
        this.name = name;
    }

    private void init(ResultSet resultSet) throws SQLException {
        checkConnection();
        initObject(resultSet);
        initStatus(resultSet);
        initProperties();
        initLists();
        objectRef = createRef();
    }

    protected abstract void initObject(ResultSet resultSet) throws SQLException;

    public void initStatus(ResultSet resultSet) throws SQLException {}

    protected void initProperties() {}

    protected void initLists() {}

    @Override
    public PsiElement getParent() {
        PsiFile containingFile = getContainingFile();
        if (containingFile != null) {
            return containingFile.getParent();
        }
        return null;
    }

    public void setContentType(DBContentType contentType) {
        this.contentType = contentType;
    }

    public DBContentType getContentType() {
        return contentType;
    }

    @Override
    public DBObjectRef getRef() {
        return objectRef;
    }

    protected DBObjectRef createRef() throws SQLException {
        checkConnection();
        return new DBObjectRef(this);
    }

    protected void checkConnection() throws SQLException {
        ConnectionHandler connectionHandler = getConnectionHandler();
        if (connectionHandler == null) throw DynamicContentLoader.DBN_INTERRUPTED_EXCEPTION;
    }

    public DBObjectProperties getProperties() {
        if (properties == null) {
            properties = new DBObjectProperties();
        }
        return properties;
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
        return parentObject == null ? null : parentObject.get();
    }

    public DBObject getDefaultNavigationObject() {
        return null;
    }

    public boolean isOfType(DBObjectType objectType) {
        return getObjectType().matches(objectType);
    }

    public String getTypeName() {
        return getObjectType().getName();
    }

    @NotNull
    public String getName() {
        return name;
    }

    @Override
    public String getQuotedName(boolean quoteAlways) {
        if (quoteAlways || needsNameQuoting()) {
            char quoteChar = DatabaseCompatibilityInterface.getInstance(this).getIdentifierQuotes();
            return quoteChar + name + quoteChar;
        } else {
            return name;
        }
    }

    public boolean needsNameQuoting() {
        return name.indexOf('-') > 0 ||
                name.indexOf('.') > 0 ||
                name.indexOf('#') > 0 ||
                getLanguageDialect(SQLLanguage.INSTANCE).isReservedWord(name);
    }

    public Icon getIcon() {
        return getObjectType().getIcon();
    }

    public String getQualifiedName() {
        return objectRef.getPath();
    }

    public String getQualifiedNameWithType() {
        return getTypeName() + " " + getQualifiedName();
    }

    public String getQualifiedNameWithConnectionId() {
        return "[" + getConnectionHandler().getId() + "]" + getQualifiedName();
    }

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
                    parentObject.getTypeName() + " " +
                    parentObject.getName() + ")";
        }
    }


    public String getToolTip() {
        if (isDisposed) {
            return null;
        }
        return new HtmlToolTipBuilder() {
            public void buildToolTip() {
                DBObjectImpl.this.buildToolTip(this);
            }
        }.getToolTip();
    }

    public void buildToolTip(HtmlToolTipBuilder ttb) {
        ttb.append(true, getQualifiedName(), false);
        ttb.append(true, "Connection: ", "-2", null, false );
        ttb.append(false, getConnectionHandler().getPresentableText(), false);
    }

    public DBObjectAttribute[] getObjectAttributes(){return null;}
    public DBObjectAttribute getNameAttribute(){return null;}

    @Override
    public DBObjectBundle getObjectBundle() {
        return getConnectionHandler().getObjectBundle();
    }

    public ConnectionHandler getConnectionHandler() {
        if (parentObject != null) {
            DBObject object = parentObject.get();
            if (object != null) {
                return object.getConnectionHandler();
            }
        } else if (objectBundle != null) {
            return objectBundle.getConnectionHandler();
        }
        return null;
    }

    @Override
    public EnvironmentType getEnvironmentType() {
        return getConnectionHandler().getEnvironmentType();
    }

    public DBLanguageDialect getLanguageDialect(DBLanguage language) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        return connectionHandler == null ?
                SQLLanguage.INSTANCE.getMainLanguageDialect() :
                connectionHandler.getLanguageDialect(language);
    }

    public DBObjectListContainer getChildObjects() {
        return childObjects;
    }

    public DBObjectRelationListContainer getChildObjectRelations() {
        return childObjectRelations;
    }

    public DBObjectListContainer initChildObjects() {
        if (childObjects == null) {
            childObjects = new DBObjectListContainer(this);
        }
        return childObjects;
    }

    public DBObjectRelationListContainer initChildObjectRelations() {
        if (childObjectRelations == null) {
            childObjectRelations = new DBObjectRelationListContainer(this);
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
        if (childObjects == null) {
            return null;
        } else {
            DBObject object = childObjects.getObject(objectType, name);
            if (object == null && lookupHidden) {
                object = childObjects.getHiddenObject(objectType, name);
            }
            return object;
        }
    }

    public DBObject getChildObject(String name, boolean lookupHidden) {
        return childObjects == null ? null :
                childObjects.getObjectForParentType(this.getObjectType(), name, lookupHidden);
    }

    public DBObject getChildObjectNoLoad(String name) {
        return childObjects == null ? null : childObjects.getObjectNoLoad(name);
    }

    public List<DBObject> getChildObjects(DBObjectType objectType) {
        if (objectType.getFamilyTypes().size() > 1) {
            List<DBObject> list = new ArrayList<DBObject>();
            for (DBObjectType childObjectType : objectType.getFamilyTypes()) {
                if (objectType != childObjectType) {
                    List<DBObject> childObjects = getChildObjects(childObjectType);
                    list.addAll(childObjects);
                } else {
                    DBObjectList<DBObject> objectList = childObjects == null ? null : childObjects.getObjectList(objectType);
                    if (objectList != null) {
                        list.addAll(objectList.getObjects());
                    }
                }
            }
            return list;
        } else {
            DBObjectList<DBObject> objectList = childObjects == null ? null : childObjects.getObjectList(objectType);
            return objectList == null ? DBObject.EMPTY_LIST : objectList.getObjects();
        }
    }

    public List<DBObjectNavigationList> getNavigationLists() {
        // todo consider caching;
        return createNavigationLists();
    }

    protected List<DBObjectNavigationList> createNavigationLists() {
        return null;
    }

    public LookupItemFactory getLookupItemFactory(DBLanguage language) {
        if (language == SQLLanguage.INSTANCE) {
            if (sqlLookupItemFactory == null) {
                sqlLookupItemFactory = new DBObjectLookupItemFactory(this, language);
            }
            return sqlLookupItemFactory;
        }
        if (language == PSQLLanguage.INSTANCE) {
            if (psqlLookupItemFactory == null) {
                psqlLookupItemFactory = new DBObjectLookupItemFactory(this, language);
            }
            return psqlLookupItemFactory;
        }
        return null;
    }

    public String extractDDL() throws SQLException {
        String ddl = null;
        CallableStatement statement = null;
        Connection connection = null;

        try {
            connection = getConnectionHandler().getPoolConnection();
            statement = connection.prepareCall("{? = call DBMS_METADATA.GET_DDL(?, ?, ?)}");
            statement.registerOutParameter(1, Types.VARCHAR);
            statement.setString(2, getTypeName().toUpperCase());
            statement.setString(3, getName());
            statement.setString(4, getParentObject().getName());

            statement.execute();
            ddl = statement.getString(1);
            ddl = ddl == null ? null : ddl.trim();
            statement.close();
        } finally{
            ConnectionUtil.closeStatement(statement);
            getConnectionHandler().freePoolConnection(connection);
        }
        return ddl;
    }

    @Nullable
    public DBObject getUndisposedElement() {
        return objectRef.get();
    }

    public DynamicContent getDynamicContent(DynamicContentType dynamicContentType) {
        if(dynamicContentType instanceof DBObjectType && childObjects != null) {
            DBObjectType objectType = (DBObjectType) dynamicContentType;
            DynamicContent dynamicContent = childObjects.getObjectList(objectType);
            if (dynamicContent == null) dynamicContent = childObjects.getHiddenObjectList(objectType);
            return dynamicContent;
        }

        else if (dynamicContentType instanceof DBObjectRelationType && childObjectRelations != null) {
            DBObjectRelationType objectRelationType = (DBObjectRelationType) dynamicContentType;
            return childObjectRelations.getObjectRelationList(objectRelationType);
        }

        return null;
    }

    public void reload() {

    }

    @NotNull
    public DatabaseObjectFile getVirtualFile() {
        if (virtualFile == null) {
            virtualFile = new DatabaseObjectFile(this);
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
        return treeChildrenLoaded;
    }

    public boolean canExpand() {
        return !isLeafTreeElement() && isTreeStructureLoaded() && getTreeChild(0).isTreeStructureLoaded();
    }

    public Icon getIcon(int flags) {
        return getIcon();
    }

    public String getPresentableText() {
        return getName();
    }

    public String getPresentableTextDetails() {
        return null;
    }

    public String getPresentableTextConditionalDetails() {
        return null;
    }

    public BrowserTreeNode getTreeParent() {
        if (parentObject != null){
            DBObject object = parentObject.get();
            if (object != null) {
                DBObjectListContainer childObjects = object.getChildObjects();
                if (childObjects != null) {
                    return childObjects.getObjectList(getObjectType());
                }
            }
        } else if (objectBundle != null) {
            return objectBundle.getObjectListContainer().getObjectList(getObjectType());
        }
        return null;
    }

    public int getTreeDepth() {
        BrowserTreeNode treeParent = getTreeParent();
        return treeParent == null ? 0 : treeParent.getTreeDepth() + 1;
    }


    @NotNull
    public synchronized List<BrowserTreeNode> getAllPossibleTreeChildren() {
        if (allPossibleTreeChildren == null) {
            allPossibleTreeChildren = buildAllPossibleTreeChildren();
        }
        return allPossibleTreeChildren;
    }

    public List<? extends BrowserTreeNode> getTreeChildren() {
        if (visibleTreeChildren == null) {
            visibleTreeChildren = new ArrayList<BrowserTreeNode>();
            visibleTreeChildren.add(new LoadInProgressTreeNode(this));

            new SimpleBackgroundTask() {
                public void run() {
                    if (!isDisposed()) buildTreeChildren();
                }
            }.start();

        }
        return visibleTreeChildren;
    }

    private void buildTreeChildren() {
        Filter<BrowserTreeNode> filter = getConnectionHandler().getObjectFilter();
        List<BrowserTreeNode> allPossibleTreeChildren = getAllPossibleTreeChildren();
        List<BrowserTreeNode> newTreeChildren = allPossibleTreeChildren;
        if (allPossibleTreeChildren.size() > 0) {
            if (!filter.acceptsAll(allPossibleTreeChildren)) {
                newTreeChildren = new ArrayList<BrowserTreeNode>();
                for (BrowserTreeNode treeNode : allPossibleTreeChildren) {
                    if (treeNode != null && filter.accepts(treeNode)) {
                        DBObjectList objectList = (DBObjectList) treeNode;
                        newTreeChildren.add(objectList);
                    }
                }
            }
            newTreeChildren = new ArrayList<BrowserTreeNode>(newTreeChildren);

            for (BrowserTreeNode treeNode : newTreeChildren) {
                DBObjectList objectList = (DBObjectList) treeNode;
                objectList.initTreeElement();
            }

            if (visibleTreeChildren.size() == 1 && visibleTreeChildren.get(0) instanceof LoadInProgressTreeNode) {
                visibleTreeChildren.get(0).dispose();
            }
        }
        visibleTreeChildren = newTreeChildren;
        treeChildrenLoaded = true;


        EventManager.notify(getProject(), BrowserTreeChangeListener.TOPIC).nodeChanged(this, TreeEventType.STRUCTURE_CHANGED);

        new ConditionalLaterInvocator() {
            public void execute() {
                if (!isDisposed()) {
                    DatabaseBrowserManager.scrollToSelectedElement(getConnectionHandler());
                }
            }
        }.start();
    }

    public void rebuildTreeChildren() {
        Filter<BrowserTreeNode> filter = getConnectionHandler().getObjectFilter();
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

    public boolean isLeafTreeElement() {
        if (!isDisposed()) {
            Filter<BrowserTreeNode> filter = getConnectionHandler().getObjectFilter();
            for (BrowserTreeNode treeNode : getAllPossibleTreeChildren() ) {
                if (treeNode != null && filter.accepts(treeNode)) {
                    return false;
                }
            }
        }
        return true;
    }

    public BrowserTreeNode getTreeChild(int index) {
        return getTreeChildren().get(index);
    }

    public int getTreeChildCount() {
        return getTreeChildren().size();
    }

    public int getIndexOfTreeChild(BrowserTreeNode child) {
        return getTreeChildren().indexOf(child);
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
        return getConnectionHandler().getProject();
    }

    public int compareTo(@NotNull Object o) {
        if (o instanceof DBObject) {
            DBObject object = (DBObject) o;
            return getRef().compareTo(object.getRef());
        }
        return -1;
    }

    public String toString() {
        return getName();
    }

    public List<PresentableProperty> getPresentableProperties() {
        List<PresentableProperty> properties = new ArrayList<PresentableProperty>();
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
        if (!isDisposed) {
            isDisposed = true;
            DisposerUtil.dispose(childObjects);
            DisposerUtil.dispose(childObjectRelations);
            CollectionUtil.clearCollection(visibleTreeChildren);
            CollectionUtil.clearCollection(allPossibleTreeChildren);
            DisposerUtil.dispose(sqlLookupItemFactory);
            DisposerUtil.dispose(psqlLookupItemFactory);
            objectBundle = null;
        }
    }


    public boolean isDisposed() {
        return isDisposed;
    }

    public String getDescription() {
        return getQualifiedName();
    }

    /*********************************************************
    *                      Navigatable                      *
    *********************************************************/
    public void navigate(boolean requestFocus) {
        DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(getProject());
        browserManager.navigateToElement(this, requestFocus);
    }

    public boolean canNavigate() {
        return true;
    }

    /*********************************************************
     *                   PsiElement                          *
     *********************************************************/

    @Override
    public PsiFile getContainingFile() throws PsiInvalidElementAccessException {
        return NavigationPsiCache.getPsiFile(this);
    }
}
