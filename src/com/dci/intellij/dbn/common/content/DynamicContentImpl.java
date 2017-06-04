package com.dci.intellij.dbn.common.content;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.content.dependency.ContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.VoidContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoadException;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.list.AbstractFiltrableList;
import com.dci.intellij.dbn.common.list.FiltrableList;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.object.common.DBVirtualObject;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.Disposer;
import gnu.trove.THashMap;

public abstract class DynamicContentImpl<T extends DynamicContentElement> extends DisposableBase implements DynamicContent<T> {
    public static final List EMPTY_CONTENT = Collections.unmodifiableList(new ArrayList(0));
    public static final List EMPTY_UNTOUCHED_CONTENT = Collections.unmodifiableList(new ArrayList(0));

    private long changeTimestamp = 0;
    private volatile boolean loading = false;
    private volatile boolean loadingInBackground = false;
    private volatile boolean loaded = false;
    private volatile boolean dirty = false;

    private GenericDatabaseElement parent;
    protected DynamicContentLoader<T> loader;
    protected ContentDependencyAdapter dependencyAdapter;
    private boolean indexed;
    private Map<String, T> index;

    protected List<T> elements = EMPTY_UNTOUCHED_CONTENT;

    protected DynamicContentImpl(@NotNull GenericDatabaseElement parent, @NotNull DynamicContentLoader<T> loader, ContentDependencyAdapter dependencyAdapter, boolean indexed) {
        this.parent = parent;
        this.loader = loader;
        this.dependencyAdapter = dependencyAdapter;
        this.indexed = indexed;
    }

    @Nullable
    public GenericDatabaseElement getParentElement() {
        return FailsafeUtil.get(parent);
    }

    @NotNull
    public ConnectionHandler getConnectionHandler() {
        return FailsafeUtil.get(getParentElement().getConnectionHandler());
    }

    public DynamicContentLoader<T> getLoader() {
        return loader;
    }

    public ContentDependencyAdapter getDependencyAdapter() {
        return dependencyAdapter;
    }

    public long getChangeTimestamp() {
        return changeTimestamp;
    }

    public boolean isLoaded() {
        return loaded;
    }

    /**
     * The content can load
     */
    public boolean canLoadFast() {
        return dependencyAdapter.canLoadFast();
    }

    @Override
    public boolean isSubContent() {
        return dependencyAdapter.isSubContent();
    }

    public boolean isLoading() {
        return loading;
    }

    public boolean isDirty() {
        return dirty || dependencyAdapter.isDirty();
    }

    public void markDirty() {
        dirty = true;
        ContentDependencyAdapter dependencyAdapter = getDependencyAdapter();
        dependencyAdapter.markSourcesDirty();
    }

    private boolean shouldReload() {
        return !isDisposed() && loaded && !loading;
    }

    private boolean shouldRefresh() {
        return !isDisposed() && /*loaded && */!loading;
    }

    public final void load(boolean force) {
        if (shouldLoad(force)) {
            synchronized (this) {
                if (shouldLoad(force)) {
                    loading = true;
                    try {
                        performLoad();
                        loaded = true;
                    } catch (InterruptedException e) {
                        setElements(EMPTY_CONTENT);
                        dirty = true;
                    } finally {
                        loading = false;
                        updateChangeTimestamp();
                    }
                }
            }
        }
    }

    public final void reload() {
        if (shouldReload()) {
            synchronized (this) {
                if (shouldReload()) {
                    loading = true;
                    try {
                        performReload();
                        List<T> elements = getAllElements();
                        for (T element : elements) {
                            checkDisposed();
                            element.refresh();
                        }
                        loaded = true;
                    } catch (InterruptedException e) {
                        setElements(EMPTY_CONTENT);
                        dirty = true;
                    } finally {
                        loading = false;
                        updateChangeTimestamp();
                    }
                }
            }
        }
    }

    @Override
    public void refresh() {
        if(shouldRefresh()) {
            synchronized (this) {
                if(shouldRefresh()) {
                    markDirty();
                }
            }
        }
    }

    @Override
    public final void loadInBackground(final boolean force) {
        if (shouldLoadInBackground(force)) {
            synchronized (this) {
                if (shouldLoadInBackground(force)) {
                    loadingInBackground = true;
                    ConnectionHandler connectionHandler = getConnectionHandler();
                    String connectionString = " (" + connectionHandler.getName() + ')';
                    new BackgroundTask(getProject(), "Loading data dictionary" + connectionString, true) {
                        @Override
                        protected void execute(@NotNull ProgressIndicator progressIndicator) {
                            try {
                                DatabaseLoadMonitor.startBackgroundLoad();
                                load(force);
                            } finally {
                                DatabaseLoadMonitor.endBackgroundLoad();
                                loadingInBackground = false;
                            }
                        }
                    }.start();
                }
            }
        }
    }

    boolean shouldLoadInBackground(boolean force) {
        return !loadingInBackground && shouldLoad(force);
    }

    private void performLoad() throws InterruptedException {
        checkDisposed();
        dependencyAdapter.beforeLoad();
        checkDisposed();
        try {
            // mark first the dirty status since dirty dependencies may
            // become valid due to parallel background load
            dirty = false;
            loader.loadContent(this, false);
        } catch (DynamicContentLoadException e) {
            dirty = !e.isModelException();
        }
        checkDisposed();
        dependencyAdapter.afterLoad();
    }

    private void performReload() throws InterruptedException {
        checkDisposed();
        dependencyAdapter.beforeReload(this);
        checkDisposed();
        try {
            checkDisposed();
            loader.reloadContent(this);
        } catch (DynamicContentLoadException e) {
            dirty = !e.isModelException();
        }
        checkDisposed();
        dependencyAdapter.afterReload(this);
    }

    public void updateChangeTimestamp() {
        changeTimestamp = System.currentTimeMillis();
    }


    /**
     * do whatever is needed after the content is loaded (e.g. refresh browser tree..)
     */
    public abstract void notifyChangeListeners();

    public void setElements(List<T> elements) {
        if (isDisposed() || elements == null || elements.size() == 0) {
            elements = EMPTY_CONTENT;
            index = null;
        } else {
            sortElements(elements);
        }
        List<T> oldElements = this.elements;
        this.elements = new AbstractFiltrableList<T>(elements) {
            @Nullable
            @Override
            public Filter<T> getFilter() {
                return DynamicContentImpl.this.getFilter();
            }
        };
        updateIndex();
        if (oldElements.size() != 0 || elements.size() != 0 ){
            notifyChangeListeners();
        }
        if (!dependencyAdapter.isSubContent() && oldElements.size() > 0 ) {
            DisposerUtil.dispose(oldElements);
        }
    }

    public void sortElements(List<T> elements) {
        Collections.sort(elements);
    }

    @NotNull
    public List<T> getElements() {
        if (!isDisposed()) {
            if (parent instanceof DBVirtualObject || isSubContent() || DatabaseLoadMonitor.isEnsureDataLoaded() || DatabaseLoadMonitor.isLoadingInBackground()) {
                load(false);
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


    protected void updateIndex() {
        if (indexed) {
            List<T> elements = this.elements;
            if (elements instanceof FiltrableList) {
                elements = ((FiltrableList) elements).getFullList();
            }
            if (elements.size() > 30) {
                if (index == null)
                    index = new THashMap<String, T>(); else
                    index.clear();

                for (T element : elements) {
                    String name = element.getName().toUpperCase();
                    index.put(name, element);
                }
            } else {
                index = null;
            }
        }
    }

    public T getElement(String name, int overload) {
        if (name != null) {
            List<T> elements = getAllElements();
            if (indexed && index != null) {
                return index.get(name.toUpperCase());
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

    public int size() {
        return getElements().size();
    }

    public boolean shouldLoad(boolean force) {
        if (loading || isDisposed()) {
            return false;
        }

        ConnectionHandler connectionHandler = getConnectionHandler();
        if (force || !loaded) {
            return dependencyAdapter.canConnect(connectionHandler);
        }

        if (isDirty()) {
            return dependencyAdapter.canLoad(connectionHandler);
        }

        return false;
    }

    public void checkDisposed() throws InterruptedException {
        if (isDisposed()) throw new InterruptedException();
    }

    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
            if (elements != EMPTY_CONTENT && elements != EMPTY_UNTOUCHED_CONTENT) {
                if (dependencyAdapter.isSubContent())
                    elements.clear(); else
                    DisposerUtil.dispose(elements);
            }
            CollectionUtil.clearMap(index);
            Disposer.dispose(dependencyAdapter);
            dependencyAdapter = VoidContentDependencyAdapter.INSTANCE;
            parent = null;
        }
    }
}
