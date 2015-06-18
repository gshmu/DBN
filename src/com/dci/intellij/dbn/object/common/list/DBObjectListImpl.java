package com.dci.intellij.dbn.object.common.list;

import javax.swing.Icon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.browser.model.BrowserTreeChangeListener;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.options.DatabaseBrowserSettings;
import com.dci.intellij.dbn.browser.options.DatabaseBrowserSortingSettings;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentImpl;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.content.dependency.ContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.sorting.DBObjectComparator;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;

public class DBObjectListImpl<T extends DBObject> extends DynamicContentImpl<T> implements DBObjectList<T> {
    private DBObjectType objectType = DBObjectType.UNKNOWN;
    private Filter<T> filter;

    public DBObjectListImpl(@NotNull DBObjectType objectType, @NotNull BrowserTreeNode treeParent, DynamicContentLoader<T> loader, ContentDependencyAdapter dependencyAdapter, boolean indexed) {
        super(treeParent, loader, dependencyAdapter, indexed);
        this.objectType = objectType;
    }

    @Override
    public boolean isFiltered() {
        return getFilter() != null;
    }

    @Nullable
    @Override
    protected Filter<T> getFilter() {
        if (filter == null) {
            return getConfigFilter();
        } else {
            return filter;
        }
    }

    @Override
    public void setQuickFilter(final Filter<T> quickFilter) {
        if (quickFilter == null) {
            filter = null;
        } else {
            filter = new Filter<T>() {
                @Override
                public boolean accepts(T object) {
                    if (quickFilter.accepts(object)) {
                        Filter<T> filter = getConfigFilter();
                        return filter == null || filter.accepts(object);
                    }
                    return false;
                }
            };
        }
    }

    @Nullable
    private Filter<T> getConfigFilter() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        return connectionHandler.isVirtual() ? null : (Filter<T>) connectionHandler.getSettings().getFilterSettings().getNameFilter(objectType);
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
        if (elements == EMPTY_CONTENT || elements == EMPTY_UNTOUCHED_CONTENT) {
            elements = new ArrayList<T>();
        }
        elements.add(object);
    }

    public T getObject(String name) {
        return getElement(name, 0);
    }

    public T getObject(String name, int overload) {
        return getElement(name, overload);
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

    @Override
    public void sortElements(List<T> elements) {
        DatabaseBrowserSettings browserSettings = DatabaseBrowserSettings.getInstance(getProject());
        DatabaseBrowserSortingSettings sortingSettings = browserSettings.getSortingSettings();
        DBObjectComparator comparator = objectType == DBObjectType.ANY ? null : sortingSettings.getComparator(objectType);
        if (comparator != null) {
            Collections.sort(elements, comparator);
        } else {
            super.sortElements(elements);
        }
    }

    @NotNull
    public String getName() {
        return objectType.getListName();
    }

    public void initTreeElement() {
        getObjects();
    }

    public Project getProject() {
        GenericDatabaseElement parent = getParentElement();
        return FailsafeUtil.get(parent.getProject());
    }

    public GenericDatabaseElement getUndisposedElement() {
        return this;
    }

    public DynamicContent getDynamicContent(DynamicContentType dynamicContentType) {
        return null;
    }

    public void notifyChangeListeners() {
        Project project = getProject();
        if (isTouched() && project != null && !project.isDisposed()) {
            EventUtil.notify(project, BrowserTreeChangeListener.TOPIC).nodeChanged(this, TreeEventType.STRUCTURE_CHANGED);
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
        return getName() + " from " + connectionHandler.getName();
    }

    /*********************************************************
     *                     TreeElement                       *
     *********************************************************/

    public boolean isTreeStructureLoaded() {
        return isTouched();
    }

    public boolean isTouched() {
        return elements != EMPTY_UNTOUCHED_CONTENT;
    }

    public boolean canExpand() {
        return isTouched() && getTreeChildCount() > 0;
    }

    public int getTreeDepth() {
        BrowserTreeNode treeParent = getTreeParent();
        return treeParent == null ? 0 : treeParent.getTreeDepth() + 1;
    }

    public BrowserTreeNode getTreeChild(int index) {
        return getTreeChildren().get(index);
    }

    @Nullable
    public BrowserTreeNode getTreeParent() {
        return (BrowserTreeNode) getParentElement();
    }

    public List<? extends BrowserTreeNode> getTreeChildren() {
        if (isLoading()) {
            return elements;
        } else {
            boolean scroll = !isTouched();
            if (!isLoaded()) {
                loadInBackground(false);
                return elements;
            }

            if (elements.size() > 0 && elements.get(0).isDisposed()) {
                loadInBackground(false);
                return elements;
            }
            if (scroll) {
                DatabaseBrowserManager.scrollToSelectedElement(getConnectionHandler());
            }
            return elements;
        }
    }

    public void refreshTreeChildren(@Nullable DBObjectType objectType) {
        if (isLoaded()) {
            if (objectType == null || this.objectType == objectType) {
                getElements();
            }

            for (DBObject object : getObjects()) {
                object.refreshTreeChildren(objectType);
            }
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
        int unfilteredElementCount = getAllElements().size();
        return elementCount > 0 ? "(" + elementCount + (elementCount != unfilteredElementCount ? "/"+ unfilteredElementCount : "") + ")" : null;
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

    /*********************************************************
     *                 ItemPresentation                      *
     *********************************************************/
    public String getLocationString() {
        return null;
    }

    public Icon getIcon(boolean open) {
        return getIcon(0);
    }

    public String toString() {
        /*if (getTreeParent() instanceof DBObject) {
            DBObject object = (DBObject) getTreeParent();
            return getName() + " of " + object.getQualifiedNameWithType();
        }*/
        return getName();
    }

    @Override
    public int compareTo(@NotNull DBObjectList objectList) {
        return objectType.compareTo(objectList.getObjectType());
    }
}
