package com.dci.intellij.dbn.common.content;

import com.dci.intellij.dbn.common.content.dependency.ContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.VoidContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoadException;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.list.AbstractFiltrableList;
import com.dci.intellij.dbn.common.list.FiltrableList;
import com.dci.intellij.dbn.common.property.PropertyHolderImpl;
import com.dci.intellij.dbn.common.thread.BackgroundMonitor;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.Synchronized;
import com.dci.intellij.dbn.common.thread.TaskInstruction;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.dci.intellij.dbn.common.content.DynamicContentStatus.*;
import static com.dci.intellij.dbn.common.thread.TaskInstructions.instructions;

public abstract class DynamicContentImpl<T extends DynamicContentElement> extends PropertyHolderImpl<DynamicContentStatus> implements DynamicContent<T> {
    protected static final List EMPTY_CONTENT = Collections.unmodifiableList(new ArrayList(0));
    protected static final List EMPTY_DISPOSED_CONTENT = Collections.unmodifiableList(new ArrayList(0));
    protected static final List EMPTY_UNTOUCHED_CONTENT = Collections.unmodifiableList(new ArrayList(0));

    private long changeTimestamp = 0;

    private GenericDatabaseElement parent;
    private ContentDependencyAdapter dependencyAdapter;
    private Map<String, T> index;

    protected List<T> elements = EMPTY_UNTOUCHED_CONTENT;

    protected DynamicContentImpl(
            @NotNull GenericDatabaseElement parent,
            ContentDependencyAdapter dependencyAdapter,
            DynamicContentStatus ... statuses) {

        this.parent = parent;
        this.dependencyAdapter = dependencyAdapter;
        if (statuses != null && statuses.length > 0) {
            for (DynamicContentStatus status : statuses) {
                set(status, true);
            }
        }
    }

    @Override
    public void compact() {
         if (elements != EMPTY_CONTENT && elements != EMPTY_UNTOUCHED_CONTENT) {
             CollectionUtil.compact(elements);
         }
    }

    @Override
    protected DynamicContentStatus[] properties() {
        return DynamicContentStatus.values();
    }

    @Override
    @NotNull
    public GenericDatabaseElement getParentElement() {
        return Failsafe.get(parent);
    }

    @Override
    @NotNull
    public ConnectionHandler getConnectionHandler() {
        return Failsafe.get(getParentElement().getConnectionHandler());
    }

    @Override
    public abstract DynamicContentLoader<T> getLoader();

    @Override
    public ContentDependencyAdapter getDependencyAdapter() {
        return dependencyAdapter;
    }

    @Override
    public long getChangeTimestamp() {
        return changeTimestamp;
    }

    @Override
    public boolean isLoaded() {
        return is(LOADED);
    }

    @Override
    public boolean isLoading() {
        return is(LOADING);
    }

    public boolean isIndexed() {
        return is(INDEXED);
    }

    /**
     * The content can load
     */
    @Override
    public boolean canLoadFast() {
        return dependencyAdapter.canLoadFast();
    }

    @Override
    public boolean isSubContent() {
        return dependencyAdapter.isSubContent();
    }

    @Override
    public boolean isDirty() {
        return is(DIRTY) || dependencyAdapter.isDirty();
    }

    @Override
    public void markDirty() {
        set(DIRTY, true);
    }

    private boolean shouldLoad() {
        if (isLoading() || isDisposed()) {
            return false;
        }

        ConnectionHandler connectionHandler = getConnectionHandler();
        if (!isLoaded()) {
            return dependencyAdapter.canConnect(connectionHandler);
        }

        if (isDirty()) {
            return dependencyAdapter.canLoad(connectionHandler);
        }

        return false;
    }

    private boolean shouldReload() {
        return !isDisposed() && isLoaded() && !isLoading();
    }

    private boolean shouldRefresh() {
        return !isDisposed() && /*loaded && */!isLoading();
    }

    @Override
    public final void load() {
        if (shouldLoad()) {
            synchronized (this) {
                if (shouldLoad()) {
                    set(LOADING, true);
                    try {
                        performLoad(false);
                        set(LOADED, true);
                    } catch (Exception e) {
                        setElements(EMPTY_CONTENT);
                        set(DIRTY, true);
                    } finally {
                        set(LOADING, false);
                        updateChangeTimestamp();
                    }
                }
            }
        }
    }

    @Override
    public final void reload() {
        if (shouldReload()) {
            synchronized (this) {
                if (shouldReload()) {
                    set(LOADING, true);
                    try {
                        performLoad(true);
                        List<T> elements = getAllElements();
                        for (T element : elements) {
                            checkDisposed();
                            element.refresh();
                        }
                        set(LOADED, true);
                    } catch (Exception e) {
                        setElements(EMPTY_CONTENT);
                        set(DIRTY, true);
                    } finally {
                        set(LOADING, false);
                        updateChangeTimestamp();
                    }
                }
            }
        }
    }

    @Override
    public void refresh() {
        if (isLoaded() && !isLoading()) {
            markDirty();
        }
    }

    @Override
    public final void loadInBackground(final boolean force) {
        Synchronized.run(this,
                () -> shouldLoadInBackground(force),
                () -> {
                    set(LOADING_IN_BACKGROUND, true);
                    ConnectionHandler connectionHandler = getConnectionHandler();
                    String connectionString = " (" + connectionHandler.getName() + ')';
                    BackgroundTask.invoke(getProject(),
                            instructions("Loading data dictionary" + connectionString, TaskInstruction.BACKGROUNDED),
                            (data, progress) -> {
                                try {
                                    load();
                                } finally {
                                    set(LOADING_IN_BACKGROUND, false);
                                }
                    });
                });
    }

    boolean shouldLoadInBackground(boolean force) {
        return isNot(LOADING_IN_BACKGROUND) && shouldLoad();
    }

    private void performLoad(boolean force) throws InterruptedException {
        checkDisposed();
        dependencyAdapter.beforeLoad(force);
        checkDisposed();
        try {
            // mark first the dirty status since dirty dependencies may
            // become valid due to parallel background load
            set(DIRTY, false);
            DynamicContentLoader<T> loader = getLoader();
            loader.loadContent(this, force);

            // refresh inner elements
            elements.forEach(t -> t.refresh());
        } catch (DynamicContentLoadException e) {
            set(DIRTY, !e.isModelException());
        }
        checkDisposed();
        dependencyAdapter.afterLoad();
    }

    @Override
    public void updateChangeTimestamp() {
        changeTimestamp = System.currentTimeMillis();
    }


    /**
     * do whatever is needed after the content is loaded (e.g. refresh browser tree..)
     */
    public abstract void notifyChangeListeners();

    @Override
    public void setElements(List<T> elements) {
        sync(CHANGING, () -> replaceElements(elements));
    }

    private void replaceElements(List<T> elements) {
        List<T> oldElements = this.elements;
        if (isDisposed() || elements == null || elements.size() == 0) {
            elements = EMPTY_CONTENT;
            index = null;
        } else {
            sortElements(elements);
        }
        this.elements = new AbstractFiltrableList<T>(elements) {
            @Nullable
            @Override
            public Filter<T> getFilter() {
                return DynamicContentImpl.this.getFilter();
            }
        };
        updateIndex();
        compact();
        if (oldElements.size() != 0 || elements.size() != 0 ){
            notifyChangeListeners();
        }
        if (!dependencyAdapter.isSubContent() && oldElements.size() > 0 ) {
            DisposerUtil.disposeInBackground(oldElements);
        }
    }

    public void sortElements(List<T> elements) {
        Collections.sort(elements);
    }

    @Override
    @NotNull
    public List<T> getElements() {
        if (!isLoaded() || shouldLoad()) {
            if (BackgroundMonitor.isBackgroundProcess() || BackgroundMonitor.isTimeoutProcess() || getDependencyAdapter().canLoadFast()) {
                synchronized (this) {
                    if (!isLoaded() || shouldLoad()) {
                        load();
                    }
                }
            } else{
                loadInBackground(false);
            }
        }
        return elements;
    }

    public List getElementsNoLoad() {
        return elements;
    }

    @NotNull
    @Override
    public List<T> getAllElements() {
        List<T> elements = getElements();
        if (elements instanceof FiltrableList) {
            FiltrableList<T> filteredElements = (FiltrableList<T>) elements;
            return filteredElements.getFullList();
        }
        return elements;
    }

    public List<T> getAllElementsNoLoad() {
        if (elements instanceof FiltrableList) {
            FiltrableList<T> filteredElements = (FiltrableList<T>) elements;
            return filteredElements.getFullList();
        }
        return elements;
    }


    private void updateIndex() {
        if (isIndexed()) {
            List<T> elements = this.elements;
            if (elements instanceof FiltrableList) {
                elements = ((FiltrableList) elements).getFullList();
            }
            if (elements.size() > 30) {
                if (index == null)
                    index = new TreeMap<>(String.CASE_INSENSITIVE_ORDER); else
                    index.clear();

                for (T element : elements) {
                    String name = element.getName();
                    index.put(name, element);
                }
            } else {
                index = null;
            }
        }
    }

    @Override
    public T getElement(String name, int overload) {
        if (name != null) {
            List<T> elements = getAllElements();
            if (/*isIndexed() && */index != null) {
                return index.get(name);
            } else {
                for (T element : elements) {
                    if (element.getName().equalsIgnoreCase(name)) {
                        if (overload == 0 || overload == element.getOverload()) {
                            return element;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    @Nullable
    public List<T> getElements(String name) {
        List<T> elements = null;
        for (T element : getAllElements()) {
            if (element.getName().equalsIgnoreCase(name)) {
                if (elements == null) {
                    elements = new ArrayList<T>();
                }
                elements.add(element);
            }
        }
        return elements;
    }

    @Override
    public int size() {
        return getElements().size();
    }

    @Override
    public boolean isDisposed() {
        return is(DISPOSED);
    }

    @Override
    public void dispose() {
        if (!isDisposed()) {
            set(DISPOSED, true);
            if (elements != EMPTY_CONTENT && elements != EMPTY_UNTOUCHED_CONTENT) {
                if (!dependencyAdapter.isSubContent()) {
                    DisposerUtil.dispose(elements);
                }
                elements = EMPTY_DISPOSED_CONTENT;
            }
            CollectionUtil.clearMap(index);
            Disposer.dispose(dependencyAdapter);
            dependencyAdapter = VoidContentDependencyAdapter.INSTANCE;
            parent = null;
        }
    }
}
