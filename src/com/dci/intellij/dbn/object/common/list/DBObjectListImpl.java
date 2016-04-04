package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.browser.model.BrowserTreeEventListener;
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
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.sorting.DBObjectComparator;
import com.dci.intellij.dbn.object.filter.quick.ObjectQuickFilter;
import com.dci.intellij.dbn.object.filter.quick.ObjectQuickFilterManager;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DBObjectListImpl<T extends DBObject> extends DynamicContentImpl<T> implements DBObjectList<T> {
    private DBObjectType objectType = DBObjectType.UNKNOWN;
    private boolean hidden;
    private InternalFilter filter;

    public DBObjectListImpl(@NotNull DBObjectType objectType, @NotNull BrowserTreeNode treeParent, DynamicContentLoader<T> loader, ContentDependencyAdapter dependencyAdapter, boolean indexed, boolean hidden) {
        super(treeParent, loader, dependencyAdapter, indexed);
        this.objectType = objectType;
        this.hidden = hidden;
        if (treeParent instanceof DBSchema && !hidden) {
            ObjectQuickFilterManager quickFilterManager = ObjectQuickFilterManager.getInstance(getProject());
            quickFilterManager.applyCachedFilter(this);
        }
    }

    @Nullable
    public static <E extends DBObject> List<E> getObjects(@Nullable DBObjectList<E> objectList) {
        return objectList == null ? null : objectList.getObjects();

    }

    @Override
    public boolean isFiltered() {
        return getFilter() != null;
    }

    @Nullable
    @Override
    public Filter<T> getFilter() {
        if (filter == null) {
            return getConfigFilter();
        } else {
            return filter;
        }
    }

    @Override
    public void setQuickFilter(ObjectQuickFilter quickFilter) {
        if (quickFilter == null) {
            filter = null;
        } else {
            filter = new InternalFilter(quickFilter);
        }
    }

    @Nullable
    @Override
    public ObjectQuickFilter getQuickFilter() {
        return filter == null ? null : filter.quickFilter;

    }

    private class InternalFilter extends Filter<T> {
        private ObjectQuickFilter quickFilter;

        public InternalFilter(ObjectQuickFilter quickFilter) {
            this.quickFilter = quickFilter;
        }

        @Override
        public boolean accepts(T object) {
            if (quickFilter.accepts(object)) {
                Filter<T> filter = getConfigFilter();
                return filter == null || filter.accepts(object);
            }
            return false;
        }
    }

    @Override
    @Nullable
    public Filter<T> getConfigFilter() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        return connectionHandler.isVirtual() ? null : (Filter<T>) connectionHandler.getSettings().getFilterSettings().getNameFilter(objectType);
    }

    @NotNull
    public List<T> getObjects() {
        return getAllElements();
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

    @NotNull
    public Project getProject() {
        GenericDatabaseElement parent = getParentElement();
        return FailsafeUtil.get(parent == null ? null : parent.getProject());
    }

    public GenericDatabaseElement getUndisposedElement() {
        return this;
    }

    @Nullable
    public DynamicContent getDynamicContent(DynamicContentType dynamicContentType) {
        return null;
    }

    public void notifyChangeListeners() {
        Project project = getProject();
        BrowserTreeNode treeParent = getTreeParent();
        if (!hidden && isTouched() && FailsafeUtil.softCheck(project) && treeParent != null && treeParent.isTreeStructureLoaded()) {
            EventUtil.notify(project, BrowserTreeEventListener.TOPIC).nodeChanged(this, TreeEventType.STRUCTURE_CHANGED);
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
            try {
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
            } catch (ProcessCanceledException e) {
                return Collections.emptyList();
            }
            return elements;
        }
    }

    public void refreshTreeChildren(@NotNull DBObjectType... objectTypes) {
        if (isLoaded()) {
            if (objectType.isOneOf(objectTypes)) {
                notifyChangeListeners();
            }

            for (DBObject object : getObjects()) {
                object.refreshTreeChildren(objectTypes);
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
        return unfilteredElementCount > 0 ? "(" + elementCount + (elementCount != unfilteredElementCount ? "/"+ unfilteredElementCount : "") + ")" : null;
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
