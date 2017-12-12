package com.dci.intellij.dbn.object.common.list;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.browser.model.BrowserTreeEventListener;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.options.DatabaseBrowserSettings;
import com.dci.intellij.dbn.browser.options.DatabaseBrowserSortingSettings;
import com.dci.intellij.dbn.common.LoggerFactory;
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
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import static com.dci.intellij.dbn.common.content.DynamicContentStatus.HIDDEN;

public class DBObjectListImpl<T extends DBObject> extends DynamicContentImpl<T> implements DBObjectList<T> {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private final DBObjectType objectType;
    private InternalFilter filter;

    DBObjectListImpl(@NotNull DBObjectType objectType, @NotNull BrowserTreeNode treeParent, DynamicContentLoader<T> loader, ContentDependencyAdapter dependencyAdapter, boolean indexed, boolean hidden) {
        super(treeParent, loader, dependencyAdapter, indexed);
        this.objectType = objectType;
        set(HIDDEN, hidden);
        if (treeParent instanceof DBSchema && !hidden) {
            ObjectQuickFilterManager quickFilterManager = ObjectQuickFilterManager.getInstance(getProject());
            quickFilterManager.applyCachedFilter(this);
        }
    }

    @Nullable
    public static <E extends DBObject> List<E> getObjects(@Nullable DBObjectList<E> objectList) {
        return objectList == null ? null : objectList.getObjects();

    }

    @Nullable
    public static <E extends DBObject> E getObject(@Nullable DBObjectList<E> objectList, String name) {
        return getObject(objectList, name, 0);
    }

    public static <E extends DBObject> E getObject(@Nullable DBObjectList<E> objectList, String name, int overload) {
        return objectList == null ? null : objectList.getObject(name, overload);
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

        InternalFilter(ObjectQuickFilter quickFilter) {
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
        return FailsafeUtil.get(parent.getProject());
    }

    public GenericDatabaseElement getUndisposedElement() {
        return this;
    }

    @Nullable
    public DynamicContent getDynamicContent(DynamicContentType dynamicContentType) {
        return null;
    }

    public void notifyChangeListeners() {
        try {
            Project project = getProject();
            BrowserTreeNode treeParent = getParent();
            if (isNot(HIDDEN) && isTouched() && FailsafeUtil.softCheck(project) && treeParent != null && treeParent.isTreeStructureLoaded()) {
                BrowserTreeEventListener treeEventListener = EventUtil.notify(project, BrowserTreeEventListener.TOPIC);
                treeEventListener.nodeChanged(this, TreeEventType.STRUCTURE_CHANGED);
            }
        } catch (ProcessCanceledException ignore) {
        } catch (Exception e) {
            LOGGER.error("Failed to notify tree change listeners", e);
        }
    }

    /*********************************************************
     *                   LoadableContent                     *
     *********************************************************/
    public String getContentDescription() {
        if (isDisposed()) {
            return "disposed";
        } else {
            if (getParent() instanceof DBObject) {
                DBObject object = (DBObject) getParent();
                return getName() + " of " + object.getQualifiedNameWithType();
            }
            ConnectionHandler connectionHandler = getConnectionHandler();
            return getName() + " from " + connectionHandler.getName();
        }
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
        return isTouched() && getChildCount() > 0;
    }

    public int getTreeDepth() {
        BrowserTreeNode treeParent = getParent();
        return treeParent == null ? 0 : treeParent.getTreeDepth() + 1;
    }

    public BrowserTreeNode getChildAt(int index) {
        return getChildren().get(index);
    }

    @Nullable
    public BrowserTreeNode getParent() {
        return (BrowserTreeNode) getParentElement();
    }

    @Override
    public int getIndex(TreeNode node) {
        return getIndex((BrowserTreeNode) node);
    }

    @Override
    public boolean getAllowsChildren() {
        return !isLeaf();
    }

    @Override
    public Enumeration children() {
        return Collections.enumeration(getChildren());
    }

    public List<? extends BrowserTreeNode> getChildren() {
        if (isLoading() || isDisposed()) {
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

    public int getChildCount() {
        return getChildren().size();
    }

    public boolean isLeaf() {
        return getChildren().size() == 0;
    }

    public int getIndex(BrowserTreeNode child) {
        return getChildren().indexOf(child);
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
        int elementCount = getChildCount();
        int unfilteredElementCount = getAllElementsNoLoad().size();
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
        browserManager.navigateToElement(this, requestFocus, true);
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
        return getName() + " - " + super.toString();
    }

    @Override
    public int compareTo(@NotNull DBObjectList objectList) {
        return objectType.compareTo(objectList.getObjectType());
    }
}
