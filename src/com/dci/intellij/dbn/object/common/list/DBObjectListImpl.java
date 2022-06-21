package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.browser.model.BrowserTreeEventListener;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.options.DatabaseBrowserSettings;
import com.dci.intellij.dbn.browser.options.DatabaseBrowserSortingSettings;
import com.dci.intellij.dbn.common.content.DynamicContentImpl;
import com.dci.intellij.dbn.common.content.DynamicContentProperty;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.content.GroupedDynamicContent;
import com.dci.intellij.dbn.common.content.dependency.ContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoaderImpl;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.filter.CompoundFilter;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.range.Range;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.util.Commons;
import com.dci.intellij.dbn.common.util.SearchAdapter;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.DatabaseEntity;
import com.dci.intellij.dbn.connection.config.ConnectionFilterSettings;
import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadata;
import com.dci.intellij.dbn.navigation.psi.DBObjectListPsiDirectory;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.DBVirtualObject;
import com.dci.intellij.dbn.object.common.sorting.DBObjectComparator;
import com.dci.intellij.dbn.object.common.sorting.SortingType;
import com.dci.intellij.dbn.object.filter.quick.ObjectQuickFilter;
import com.dci.intellij.dbn.object.filter.quick.ObjectQuickFilterManager;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static com.dci.intellij.dbn.common.content.DynamicContentProperty.*;
import static com.dci.intellij.dbn.common.util.Commons.nvl;
import static com.dci.intellij.dbn.common.util.Search.binarySearch;
import static com.dci.intellij.dbn.common.util.Search.comboSearch;
import static com.dci.intellij.dbn.object.common.DBObjectSearchAdapters.binary;
import static com.dci.intellij.dbn.object.common.DBObjectSearchAdapters.linear;
import static com.dci.intellij.dbn.object.type.DBObjectType.*;

@Getter
@Setter
public class DBObjectListImpl<T extends DBObject> extends DynamicContentImpl<T> implements DBObjectList<T> {
    private final DBObjectType objectType;
    private ObjectQuickFilter<T> quickFilter;
    private volatile PsiDirectory psiDirectory;

    DBObjectListImpl(
            @NotNull DBObjectType objectType,
            @NotNull DatabaseEntity parent,
            ContentDependencyAdapter dependencyAdapter,
            DynamicContentProperty... properties) {
        super(parent, dependencyAdapter, properties);
        this.objectType = objectType;
        if ((parent instanceof DBSchema || parent instanceof DBObjectBundle) && !isInternal()) {
            ObjectQuickFilterManager quickFilterManager = ObjectQuickFilterManager.getInstance(getProject());
            quickFilterManager.restoreQuickFilter(this);
        }
        //DBObjectListLoaderRegistry.register(treeParent, objectType, loader);
    }

    @Override
    public DynamicContentLoader<T, DBObjectMetadata> getLoader() {
        BrowserTreeNode parent = getParent();
        if (parent instanceof DBVirtualObject) {
            return DynamicContentLoader.VOID_CONTENT_LOADER;
        } else {
            DynamicContentType parentContentType = parent.getDynamicContentType();
            return DynamicContentLoaderImpl.resolve(parentContentType, objectType);
        }
    }

    @Nullable
    public static <E extends DBObject> List<E> getObjects(@Nullable DBObjectList<E> objectList) {
        return objectList == null ? null : objectList.getObjects();

    }

    @Nullable
    public static <E extends DBObject> E getObject(@Nullable DBObjectList<E> objectList, String name) {
        return getObject(objectList, name, (short) 0);
    }

    public static <E extends DBObject> E getObject(@Nullable DBObjectList<E> objectList, String name, short overload) {
        return objectList == null ? null : objectList.getObject(name, overload);
    }

    @Nullable
    @Override
    public Filter<T> getFilter() {
        Filter<T> configFilter = getConfigFilter();
        if (configFilter != null && this.quickFilter != null) {
            return CompoundFilter.of(configFilter, this.quickFilter);

        } else if (configFilter != null) {
            return configFilter;

        } else {
            return this.quickFilter;
        }
    }

    @Override
    @Nullable
    public Filter<T> getConfigFilter() {
        ConnectionHandler connection = this.getConnection();
        if (Failsafe.check(connection) && !connection.isVirtual()) {
            ConnectionFilterSettings filterSettings = connection.getSettings().getFilterSettings();
            return filterSettings.getNameFilter(objectType);
        }
        return null;
    }

    @Override
    @NotNull
    public List<T> getObjects() {
        return getAllElements();
    }

    @Override
    public void collectObjects(Consumer<? super DBObject> consumer) {
        for (T object : getAllElements()) {
            consumer.accept(object);
        }
    }

    @Override
    public List<T> getObjects(String name) {
        return getElements(name);
    }

    @Override
    public void addObject(T object) {
        if (elements == EMPTY_CONTENT || elements == EMPTY_UNTOUCHED_CONTENT) {
            elements = new ArrayList<>();
        }

        if (!elements.contains(object)) {
            elements.add(object);
        }
    }

    @Override
    public T getObject(String name) {
        return getElement(name, (short) 0);
    }

    @Override
    public T getObject(String name, short overload) {
        return getElement(name, overload);
    }

    @Override
    public T getElement(String name, short overload) {
        if (name != null) {
            List<T> elements = getAllElements();
            if (!elements.isEmpty()) {
                if (objectType == ARGUMENT || objectType == TYPE_ATTRIBUTE) {
                    // arguments and type attributes are sorted by position (linear search)
                    return super.getElement(name, overload);

                } else if (objectType == TYPE) {
                    return Commons.coalesce(
                            () -> binarySearch(elements, binary(name, overload, false)),
                            () -> binarySearch(elements, binary(name, overload, true)));

                } else if (isSearchable()) {
                    if (objectType == COLUMN) {
                        // primary key columns are sorted by position at beginning ot the list of elements
                        SearchAdapter<T> linear = linear(name, c -> c instanceof DBColumn && ((DBColumn) c).isPrimaryKey());
                        SearchAdapter<T> binary = binary(name);
                        return comboSearch(elements, linear, binary);
                    }  else {
                        SearchAdapter<T> binary = objectType.isOverloadable() ?
                                binary(name, overload) :
                                binary(name);

                        return binarySearch(elements, binary);

                    }
                } else {
                    return super.getElement(name, overload);
                }
            }
        }
        return null;
    }


    @Override
    public boolean isInternal() {
        return is(INTERNAL);
    }

    @Override
    public boolean isHidden() {
        return is(HIDDEN);
    }

    @Override
    public boolean isDependency() {
        return is(DEPENDENCY);
    }

    private boolean isSearchable() {
        return is(SEARCHABLE);
    }

    @Override
    protected void sortElements(List<T> elements) {
        if (isInternal()) {
            if (is(GROUPED) || true ) { // TODO binary search on grouped elements
                super.sortElements(elements);
            } else {
                val comparator = DBObjectComparator.basic(objectType);
                elements.sort(comparator);
                set(SEARCHABLE, true);
            }
        } else {
            DatabaseBrowserSettings browserSettings = DatabaseBrowserSettings.getInstance(getProject());
            DatabaseBrowserSortingSettings sortingSettings = browserSettings.getSortingSettings();
            val comparator = objectType == ANY ? null : sortingSettings.getComparator(objectType);

            if (comparator != null) {
                elements.sort(comparator);
                boolean searchable = comparator.getSortingType() == SortingType.NAME;
                set(SEARCHABLE, searchable);
            } else {
                super.sortElements(elements);
                set(SEARCHABLE, true);
            }

        }
    }

    @Override
    @NotNull
    public String getName() {
        return objectType.getListName();
    }

    @Override
    public void initTreeElement() {
        if (!isLoading() && !isLoaded()) {
            getObjects();
        }
    }

    @Override
    @NotNull
    public Project getProject() {
        DatabaseEntity parent = getParentEntity();
        return Failsafe.nn(parent.getProject());
    }

    @Override
    public PsiDirectory getPsiDirectory() {
        if (psiDirectory == null) {
            synchronized (this) {
                if (psiDirectory == null) {
                    Failsafe.nd(this);
                    psiDirectory = new DBObjectListPsiDirectory(this);
                }
            }
        }
        return psiDirectory;
    }



    @Override
    public void notifyChangeListeners() {
        try {
            Project project = getProject();
            BrowserTreeNode treeParent = getParent();
            if (!isInternal() && isTouched() && Failsafe.check(project) && treeParent.isTreeStructureLoaded()) {
                ProjectEvents.notify(project,
                        BrowserTreeEventListener.TOPIC,
                        (listener) -> listener.nodeChanged(this, TreeEventType.STRUCTURE_CHANGED));
            }
        } catch (ProcessCanceledException ignore) {}
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
            ConnectionHandler connection = this.getConnection();
            return getName() + " from " + connection.getName();
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
        return getParentEntity();
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
    public Enumeration<? extends BrowserTreeNode> children() {
        return Collections.enumeration(getChildren());
    }

    @Override
    public List<? extends BrowserTreeNode> getChildren() {
        try {
            if (!isLoading() && !isDisposed()) {
                boolean scroll = !isTouched();
                if (!isLoaded() || isDirty()) {
                    loadInBackground();
                    scroll = false;
                }

                if (scroll) {
                    ConnectionHandler connection = this.getConnection();
                    DatabaseBrowserManager.scrollToSelectedElement(connection);
                }
            }
        } catch (ProcessCanceledException ignore) {}

        return elements;
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
        return getChildren().isEmpty();
    }

    @Override
    public int getIndex(BrowserTreeNode child) {
        return getChildren().indexOf(child);
    }

    @Override
    public DynamicContentType getContentType() {
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
        if (isDisposed()) {
            return getName() + " - " + super.toString();
        }

        /*if (getTreeParent() instanceof DBObject) {
            DBObject object = (DBObject) getTreeParent();
            return getName() + " of " + object.getQualifiedNameWithType();
        }*/
        return getParentEntity().getDynamicContentType() + " (" + getParentEntity().getName() + ") " + getName() + " - " + super.toString();
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

    @Override
    public void sort(DBObjectComparator<T> comparator) {
        if (elements.size() > 1) {
            elements.sort(comparator);
            set(SEARCHABLE, comparator.getSortingType() == SortingType.NAME);
        }
    }

    public static class Grouped<T extends DBObject> extends DBObjectListImpl<T> implements GroupedDynamicContent<T> {
        private Map<DBObjectType, Range> parentTypeRanges;
        private Map<String, Range> parentNameRanges;

        Grouped(
                @NotNull DBObjectType objectType,
                @NotNull DatabaseEntity parent,
                ContentDependencyAdapter dependencyAdapter,
                DynamicContentProperty... statuses) {
            super(objectType, parent, dependencyAdapter, statuses);
            set(GROUPED, true);
        }


        @Override
        protected void afterUpdate() {
            parentTypeRanges = new HashMap<>();
            parentNameRanges = new HashMap<>();

            List<T> elements = getAllElements();
            if (!elements.isEmpty()) {
                DBObjectType currentParentType = null;
                String currentParentName = null;
                int currentTypeOffset = 0;
                int currentNameOffset = 0;
                for (int i = 0; i < elements.size(); i++) {
                    T object = elements.get(i);
                    DBObject parentObject = object.getParentObject();
                    DBObjectType parentType = parentObject.getObjectType();
                    String parentName = parentObject.getName();

                    currentParentType = nvl(currentParentType, parentType);
                    currentParentName = nvl(currentParentName, parentName);

                    if (currentParentType != parentType) {
                        parentTypeRanges.put(currentParentType, new Range(currentTypeOffset, i - 1));
                        currentParentType = parentType;
                        currentTypeOffset = i;
                    }

                    if (!Objects.equals(currentParentName, parentName)) {
                        parentNameRanges.put(currentParentName, new Range(currentNameOffset, i - 1));
                        currentParentName = parentName;
                        currentNameOffset = i;
                    }


                    if (i == elements.size() - 1) {
                        parentTypeRanges.put(currentParentType, new Range(currentTypeOffset, i));
                        parentNameRanges.put(currentParentName, new Range(currentNameOffset, i));
                    }
                }
            }
        }

        public List<T> getChildElements(String parentName) {
            if (parentNameRanges != null) {
                Range range = parentNameRanges.get(parentName);
                if (range != null) {
                    return getAllElements().subList(range.getLeft(), range.getRight() + 1);
                }
            }
            return Collections.emptyList();
        }

        @Override
        public T getElement(String name, short overload) {
            if (parentNameRanges != null) {
                SearchAdapter<T> adapter = getObjectType().isOverloadable() ?
                        binary(name, overload) :
                        binary(name);
                Collection<Range> ranges = parentNameRanges.values();
                for (Range range : ranges) {
                    T element = binarySearch(elements, range.getLeft(), range.getRight(), adapter);
                    if (element != null) {
                        return element;
                    }
                }
            }
            return null;
        }
    }
}
