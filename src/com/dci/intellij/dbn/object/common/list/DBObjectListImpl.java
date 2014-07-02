package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.browser.model.BrowserTreeChangeListener;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.code.sql.color.SQLTextAttributesKeys;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentImpl;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.content.dependency.ContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatus;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import java.util.ArrayList;
import java.util.List;

public class DBObjectListImpl<T extends DBObject> extends DynamicContentImpl<T> implements DBObjectList<T> {
    private boolean isHidden;
    private boolean isTouched;

    private DBObjectType objectType = DBObjectType.UNKNOWN;

    public DBObjectListImpl(DBObjectType objectType, BrowserTreeNode treeParent, DynamicContentLoader<T> loader, ContentDependencyAdapter dependencyAdapter, boolean indexed) {
        super(treeParent, loader, dependencyAdapter, indexed);
        this.objectType = objectType;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean hidden) {
        isHidden = hidden;
    }

    @Override
    public Filter getFilter() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        return connectionHandler == null ? null :
                connectionHandler.getSettings().getFilterSettings().getObjectNameFilterSettings().getFilter(objectType);
    }

    @NotNull
    public List<T> getObjects() {
        return getElements();
    }

    @Override
    public List<T> getObjects(String name) {
        return getElements(name);
    }

    public void addObject(T object) {
        if (elements == DynamicContentImpl.EMPTY_LIST) {
            elements = new ArrayList<T>();
        }
        elements.add(object);
    }

    public T getObject(String name) {
        return getElement(name);
    }

    public T getObject(String name, String parentName) {
        for (T element : elements) {
            String elementName = element.getName();
            String elementParentName = element.getParentObject().getName();

            if (elementName.equalsIgnoreCase(name) &&
                    elementParentName.equalsIgnoreCase(parentName)) {
                return element;
            }
        }
        return null;
    }



    public String getName() {
        return objectType.getListName();
    }

    public void initTreeElement() {
        getObjects();
    }

    public Project getProject() {
        return getParent().getProject();
    }

    public GenericDatabaseElement getUndisposedElement() {
        return this;
    }

    public DynamicContent getDynamicContent(DynamicContentType dynamicContentType) {
        return null;
    }

    public void notifyChangeListeners() {
        if (isTouched) {
            EventManager.notify(getProject(), BrowserTreeChangeListener.TOPIC).nodeChanged(this, TreeEventType.STRUCTURE_CHANGED);
        }
    }

    /*********************************************************
     *                   LoadableContent                     *
     *********************************************************/
    public String getContentDescription() {
        if (getTreeParent() instanceof DBObject) {
            DBObject object = (DBObject) getTreeParent();
            return getName() + " of " + object.getQualifiedNameWithType();
        }
        ConnectionHandler connectionHandler = getConnectionHandler();
        return connectionHandler == null ?
                getName() :
                getName() + " from " + connectionHandler.getName() ;
    }

    /*********************************************************
     *                     TreeElement                       *
     *********************************************************/

    public boolean isTreeStructureLoaded() {
        return isTouched;
    }

    public boolean canExpand() {
        return isTouched && getTreeChildCount() > 0;
    }

    public int getTreeDepth() {
        BrowserTreeNode treeParent = getTreeParent();
        return treeParent == null ? 0 : treeParent.getTreeDepth() + 1;
    }

    public BrowserTreeNode getTreeChild(int index) {
        return getTreeChildren().get(index);
    }

    public BrowserTreeNode getTreeParent() {
        return (BrowserTreeNode) getParent();
    }

    public List<? extends BrowserTreeNode> getTreeChildren() {
        if (isLoading()) {
            return elements;
        } else {
            if (!isTouched) {
                load(false);
                isTouched = true;
                DatabaseBrowserManager.scrollToSelectedElement(getConnectionHandler());
            }
            return elements;
        }
    }

    public void rebuildTreeChildren() {
        if (isLoaded()) {
            for (DBObject object : getObjects()) {
                object.rebuildTreeChildren();
            }
        }
    }

    public int getTreeChildCount() {
        return getTreeChildren().size();
    }

    public boolean isLeafTreeElement() {
        return getTreeChildren().size() == 0;
    }

    public int getIndexOfTreeChild(BrowserTreeNode child) {
        return getTreeChildren().indexOf(child);
    }


    public DBObjectType getObjectType() {
        return objectType;
    }

    public Icon getIcon(int flags) {
        return objectType.getListIcon();
    }

    public String getPresentableText() {
        return objectType.getPresentableListName();
    }

    public String getPresentableTextDetails() {
        int elementCount = getTreeChildCount();
        return elementCount > 0 ? "(" + elementCount + ")" : null;
    }

    public String getPresentableTextConditionalDetails() {
        return null;
    }

    /*********************************************************
    *                    ToolTipProvider                    *
    *********************************************************/
    public String getToolTip() {
        return null;
    }

    /*********************************************************
     *                  NavigationItem                       *
     *********************************************************/
    public void navigate(boolean requestFocus) {
        DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(getProject());
        browserManager.navigateToElement(this, requestFocus);
    }

    public boolean canNavigate() {
        return false;
    }

    public boolean canNavigateToSource() {
        return false;
    }

    public ItemPresentation getPresentation() {
        return this;
    }

    public FileStatus getFileStatus() {
        return FileStatus.NOT_CHANGED;
    }

    /*********************************************************
     *                 ItemPresentation                      *
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

    public String toString() {
        /*if (getTreeParent() instanceof DBObject) {
            DBObject object = (DBObject) getTreeParent();
            return getName() + " of " + object.getQualifiedNameWithType();
        }*/
        return getName();
    }

}
