package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.browser.model.BrowserTreeEventListener;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.options.DatabaseBrowserSettings;
import com.dci.intellij.dbn.browser.options.DatabaseBrowserSortingSettings;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.content.DynamicContentImpl;
import com.dci.intellij.dbn.common.content.DynamicContentStatus;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.content.dependency.ContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoaderImpl;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.connection.config.ConnectionFilterSettings;
import com.dci.intellij.dbn.navigation.psi.DBObjectListPsiDirectory;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBVirtualObject;
import com.dci.intellij.dbn.object.common.sorting.DBObjectComparator;
import com.dci.intellij.dbn.object.filter.quick.ObjectQuickFilter;
import com.dci.intellij.dbn.object.filter.quick.ObjectQuickFilterManager;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static com.dci.intellij.dbn.common.content.DynamicContentStatus.INTERNAL;

public class DBObjectListImpl<T extends DBObject> extends DynamicContentImpl<T> implements DBObjectList<T> {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private final DBObjectType objectType;
    private InternalFilter filter;
    private PsiDirectory psiDirectory;

    DBObjectListImpl(
            @NotNull DBObjectType objectType,
            @NotNull BrowserTreeNode treeParent,
            ContentDependencyAdapter dependencyAdapter,
            DynamicContentStatus... statuses) {
        super(treeParent, dependencyAdapter, statuses);
        this.objectType = objectType;
        if (treeParent instanceof DBSchema && !isInternal()) {
            ObjectQuickFilterManager quickFilterManager = ObjectQuickFilterManager.getInstance(getProject());
            quickFilterManager.applyCachedFilter(this);
        }
        //DBObjectListLoaderRegistry.register(treeParent, objectType, loader);
    }

    @Override
    public DynamicContentLoader<T> getLoader() {
        BrowserTreeNode parent = getParent();
        if (parent instanceof DBVirtualObject) {
            return DynamicContentLoader.VOID_CONTENT_LOADER;
        } else {
            DynamicContentType parentContentType = parent.getDynamicContentType();
            return DynamicContentLoaderImpl.resolve(parentContentType, objectType);
        }
    }

    @Override
    public boolean isInternal() {
        return is(INTERNAL);
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

    private class InternalFilter implements Filter<T> {
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
        if (Failsafe.check(connectionHandler) && !connectionHandler.isVirtual()) {
            ConnectionFilterSettings filterSettings = connectionHandler.getSettings().getFilterSettings();
            return (Filter<T>) filterSettings.getNameFilter(objectType);
        }
        return null;
    }

    @Override
    @NotNull
    public List<T> getObjects() {
        return getAllElements();
    }

    @Override
    public List<T> getObjects(String name) {
        return getElements(name);
    }

    @Override
    public void addObject(T object) {
        if (elements == EMPTY_CONTENT || elements == EMPTY_UNTOUCHED_CONTENT) {
            elements = new ArrayList<T>();
        }
        elements.add(object);
    }

    @Override
    public T getObject(String name) {
        return getElement(name, 0);
    }

    @Override
    public T getObject(String name, int overload) {
        return getElement(name, overload);
    }

    @Override
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
        DBObjectComparator<T> comparator =
                objectType == DBObjectType.ANY ? null :
                        sortingSettings.getComparator(objectType);

        if (comparator != null) {
            elements.sort(comparator);
        } else {
            super.sortElements(elements);
        }
    }

    @Override
    @NotNull
    public String getName() {
        return objectType.getListName();
    }

    @Override
    public void initTreeElement() {
        getObjects();
    }

    @Override
    @NotNull
    public Project getProject() {
        GenericDatabaseElement parent = getParentElement();
        return Failsafe.get(parent.getProject());
    }

    @Override
    public GenericDatabaseElement getUndisposedElement() {
        return this;
    }

    @Override
    public PsiDirectory getPsiDirectory() {
        if (psiDirectory == null) {
            synchronized (this) {
                if (psiDirectory == null) {
                    Failsafe.ensure(this);
                    psiDirectory = new DBObjectListPsiDirectory(this);
                }
            }
        }
        return psiDirectory;
    }

    @Override
    public void notifyChangeListeners() {
        Failsafe.lenient(() -> {
            Project project = getProject();
            BrowserTreeNode treeParent = getParent();
            if (isNot(INTERNAL) && isTouched() && Failsafe.check(project) && treeParent.isTreeStructureLoaded()) {
                EventUtil.notify(project,
                        BrowserTreeEventListener.TOPIC,
                        (listener) -> listener.nodeChanged(this, TreeEventType.STRUCTURE_CHANGED));
            }
        });
    }

    /*********************************************************
     *                   LoadableContent                     *
     *********************************************************/
    @Override
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

    @Override
    public boolean isTreeStructureLoaded() {
        return isTouched();
    }

    public boolean isTouched() {
        return elements != EMPTY_UNTOUCHED_CONTENT;
    }

    @Override
    public boolean canExpand() {
        return isTouched() && getChildCount() > 0;
    }

    @Override
    public int getTreeDepth() {
        BrowserTreeNode treeParent = getParent();
        return treeParent.getTreeDepth() + 1;
    }

    @Override
    public BrowserTreeNode getChildAt(int index) {
        return getChildren().get(index);
    }

    @Override
    @NotNull
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

    @Override
    public List<? extends BrowserTreeNode> getChildren() {
        if (isLoading() || isDisposed()) {
            return elements;
        } else {
            return Failsafe.lenient(Collections.emptyList(), () -> {
                boolean scroll = !isTouched();
                if (!isLoaded() || isDirty()) {
                    loadInBackground();
                    scroll = false;
                }

                if (scroll) {
                    ConnectionHandler connectionHandler = getConnectionHandler();
                    DatabaseBrowserManager.scrollToSelectedElement(connectionHandler);
                }
                return elements;
            });
        }
    }

    @Override
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

    @Override
    public void rebuildTreeChildren() {
        if (isLoaded()) {
            for (DBObject object : getObjects()) {
                object.rebuildTreeChildren();
            }
        }
    }

    @Override
    public int getChildCount() {
        return getChildren().size();
    }

    @Override
    public boolean isLeaf() {
        return getChildren().size() == 0;
    }

    @Override
    public int getIndex(BrowserTreeNode child) {
        return getChildren().indexOf(child);
    }


    @Override
    public DBObjectType getObjectType() {
        return objectType;
    }

    @Override
    public Icon getIcon(int flags) {
        return objectType.getListIcon();
    }

    @Override
    public String getPresentableText() {
        return objectType.getPresentableListName();
    }

    @Override
    public String getPresentableTextDetails() {
        int elementCount = getChildCount();
        int unfilteredElementCount = getAllElementsNoLoad().size();
        return unfilteredElementCount > 0 ? "(" + elementCount + (elementCount != unfilteredElementCount ? "/"+ unfilteredElementCount : "") + ")" : null;
    }

    @Override
    public String getPresentableTextConditionalDetails() {
        return null;
    }

    /*********************************************************
    *                    ToolTipProvider                    *
    *********************************************************/
    @Override
    public String getToolTip() {
        return null;
    }

    /*********************************************************
     *                  NavigationItem                       *
     *********************************************************/
    @Override
    public void navigate(boolean requestFocus) {
        DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(getProject());
        browserManager.navigateToElement(this, requestFocus, true);
    }

    @Override
    public boolean canNavigate() {
        return false;
    }

    @Override
    public boolean canNavigateToSource() {
        return false;
    }

    @Override
    public ItemPresentation getPresentation() {
        return this;
    }

    /*********************************************************
     *                 ItemPresentation                      *
     *********************************************************/
    @Override
    public String getLocationString() {
        return null;
    }

    @Override
    public Icon getIcon(boolean open) {
        return getIcon(0);
    }

    public String toString() {
        /*if (getTreeParent() instanceof DBObject) {
            DBObject object = (DBObject) getTreeParent();
            return getName() + " of " + object.getQualifiedNameWithType();
        }*/
        return getParentElement().getDynamicContentType() + " (" + getParentElement().getName() + ") " + getName() + " - " + super.toString();
    }

    @Override
    public int compareTo(@NotNull DBObjectList objectList) {
        return objectType.compareTo(objectList.getObjectType());
    }

    @Override
    public void disposeInner() {
        psiDirectory = null;
        super.disposeInner();
    }
}
