package com.dci.intellij.dbn.common.content;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.content.dependency.ContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.VoidContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.SafeDisposer;
import com.dci.intellij.dbn.common.list.FilteredList;
import com.dci.intellij.dbn.common.notification.NotificationGroup;
import com.dci.intellij.dbn.common.notification.NotificationSupport;
import com.dci.intellij.dbn.common.property.DisposablePropertyHolder;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.thread.ThreadMonitor;
import com.dci.intellij.dbn.common.thread.ThreadProperty;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.dci.intellij.dbn.common.util.Numbers;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Collections;
import java.util.List;

import static com.dci.intellij.dbn.common.content.DynamicContentStatus.*;

public abstract class DynamicContentImpl<T extends DynamicContentElement>
        extends DisposablePropertyHolder<DynamicContentStatus>
        implements DynamicContent<T>,
                   NotificationSupport {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    protected static final List EMPTY_CONTENT = java.util.Collections.unmodifiableList(Collections.emptyList());
    protected static final List EMPTY_DISPOSED_CONTENT = java.util.Collections.unmodifiableList(Collections.emptyList());
    protected static final List EMPTY_UNTOUCHED_CONTENT = java.util.Collections.unmodifiableList(Collections.emptyList());

    private short changeSignature = 0;

    private GenericDatabaseElement parent;
    private ContentDependencyAdapter dependencyAdapter;

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
             elements = CollectionUtil.compact(elements);
         }
    }

    @Override
    protected DynamicContentStatus[] properties() {
        return DynamicContentStatus.values();
    }

    @Override
    @NotNull
    public GenericDatabaseElement getParentElement() {
        return Failsafe.nn(parent);
    }

    @NotNull
    public ConnectionId getConnectionId() {
        return getParentElement().getConnectionId();
    }

    @Override
    @NotNull
    public ConnectionHandler getConnectionHandler() {
        return Failsafe.nn(getParentElement().getConnectionHandler());
    }

    @Override
    public abstract DynamicContentLoader<T, ?> getLoader();

    @Override
    public ContentDependencyAdapter getDependencyAdapter() {
        return dependencyAdapter;
    }

    @Override
    public short getChangeSignature() {
        return changeSignature;
    }

    @Override
    public boolean isLoaded() {
        return is(LOADED);
    }

    @Override
    public boolean isLoading() {
        return is(LOADING);
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
        return is(DIRTY);
    }

    @Override
    public boolean isMutable() {
        return is(MUTABLE);
    }

    @Override
    public boolean isPassive() {
        return is(PASSIVE);
    }

    @Override
    public void markDirty() {
        set(DIRTY, true);
    }

    private boolean shouldLoad() {
        if (isDisposed() || isLoading()) {
            return false;
        }

        if (isPassive()) {
            return true;
        }

        ConnectionHandler connectionHandler = getConnectionHandler();
        if (!isLoaded()) {
            return dependencyAdapter.canConnect(connectionHandler);
        }

        if (isDirty() || dependencyAdapter.areDependenciesDirty()) {
            return dependencyAdapter.canLoad(connectionHandler);
        }

        return false;
    }

    private boolean shouldReload() {
        if (isDisposed() || isLoading()) {
            return false;
        }

        if(isPassive()) {
            return true;
        }

        return isLoaded();
    }

    private boolean shouldRefresh() {
        return !isDisposed() && isLoaded() && !isLoading() && !is(REFRESHING);
    }

    private boolean shouldLoadInBackground() {
        return shouldLoad() && !is(LOADING_IN_BACKGROUND);
    }

    @Override
    public final void load() {
        if (shouldLoad()) {
            set(LOADING, true);
            try {
                performLoad(false);
            } finally {
                set(LOADING, false);
                updateChangeSignature();
            }
        }
    }

    @Override
    public final void reload() {
        if (shouldReload()) {
            set(LOADING, true);
            try {
                performLoad(true);
            } finally {
                set(LOADING, false);
                updateChangeSignature();
            }
            CollectionUtil.forEach(elements,
                    (element) -> {
                        checkDisposed();
                        element.refresh();
                    });
        }
    }

    @Override
    public void ensure() {
        if (!isLoaded() || shouldLoad()) {
            synchronized (this) {
                if (!isLoaded() || shouldLoad()) {
                    load();
                }
            }
        }
    }

    @Override
    public void refresh() {
        if (shouldRefresh()) {
            try {
                set(REFRESHING, true);
                markDirty();
                dependencyAdapter.refreshSources();
                if (!is(INTERNAL)){
                    CollectionUtil.forEach(elements,
                            (element) -> {
                                checkDisposed();
                                element.refresh();
                            });
                }
            } finally {
                set(REFRESHING, false);
            }
        }
    }

    @Override
    public final void loadInBackground() {
        if (shouldLoadInBackground()) {
            set(LOADING_IN_BACKGROUND, true);
            ConnectionHandler connectionHandler = getConnectionHandler();
            Progress.background(
                    getProject(),
                    connectionHandler.getMetaLoadTitle(), false,
                    (progress) -> {
                        try{
                            progress.setText("Loading " + getContentDescription());
                            ensure();
                        } finally {
                            set(LOADING_IN_BACKGROUND, false);
                        }
                    });
        }
    }

    private void performLoad(boolean force) {
        //System.out.println( this + " :invoked by " + ThreadMonitor.current());
        checkDisposed();
        dependencyAdapter.beforeLoad(force);
        checkDisposed();

        try {
            // mark first the dirty status since dirty dependencies may
            // become valid due to parallel background load
            set(DIRTY, false);
            DynamicContentLoader<T, ?> loader = getLoader();
            loader.loadContent(this, force);
            set(LOADED, true);

            // refresh inner elements
            if (force) elements.forEach(t -> t.refresh());

        } catch (ProcessCanceledException e) {
            throw e;

        } catch (SQLFeatureNotSupportedException e) {
            // unsupported feature: log in notification area
            elements = EMPTY_CONTENT;
            set(LOADED, true);
            set(ERROR, true);
            sendWarningNotification(
                    NotificationGroup.METADATA,
                    "Failed to load {0}. Feature not supported: {1}",
                    getContentDescription(), e);

        } catch (SQLException e) {
            // connectivity / timeout exceptions: mark content dirty (no logging)
            elements = EMPTY_CONTENT;
            set(DIRTY, true);

        } catch (Throwable e) {
            // any other exception: log error
            LOGGER.error("Failed to load content", e);
            elements = EMPTY_CONTENT;
            set(DIRTY, true);
        }

        checkDisposed();
        dependencyAdapter.afterLoad();
    }

    @Override
    public void updateChangeSignature() {
        changeSignature = Numbers.timeSignature();
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
        } else {
            sortElements(elements);
        }
        this.elements = FilteredList.stateless(getFilter(), elements);
        compact();
        if (oldElements.size() != 0 || elements.size() != 0 ){
            notifyChangeListeners();
        }
        if (is(MASTER)) {
            SafeDisposer.dispose(oldElements, false, true);
        }
    }

    public void sortElements(List<T> elements) {
        elements.sort(null);
    }

    @Override
    @NotNull
    public List<T> getElements() {
        if (getDependencyAdapter().canLoadFast() ||
                ThreadMonitor.is(
                        ThreadProperty.PROGRESS,
                        ThreadProperty.BACKGROUND,
                        ThreadProperty.TIMEOUT/*,
                        ThreadProperty.CODE_ANNOTATING*/)) {
            ensure();
        } else{
            loadInBackground();
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
        if (elements instanceof FilteredList) {
            FilteredList<T> filteredElements = (FilteredList<T>) elements;
            return filteredElements.getBase();
        }
        return elements;
    }

    public List<T> getAllElementsNoLoad() {
        if (elements instanceof FilteredList) {
            FilteredList<T> filteredElements = (FilteredList<T>) elements;
            return filteredElements.getBase();
        }
        return elements;
    }

    @Override
    public T getElement(String name, short overload) {
        if (name != null) {
            List<T> elements = getAllElements();
            return CollectionUtil.first(elements,
                    (element) -> matchElement(element, name, overload));
        }
        return null;
    }

    protected boolean matchElement(T element, String name, short overload) {
        return (overload == 0 || overload == element.getOverload()) &&
                StringUtil.equalsIgnoreCase(element.getName(), name);
    }

    @Override
    @Nullable
    public List<T> getElements(String name) {
        return CollectionUtil.filter(getAllElements(), false, false, (element) -> StringUtil.equalsIgnoreCase(element.getName(), name));
    }

    @Override
    public int size() {
        return getElements().size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public void disposeInner() {
        if (elements != EMPTY_CONTENT && elements != EMPTY_UNTOUCHED_CONTENT) {
            if (!dependencyAdapter.isSubContent()) {
                SafeDisposer.dispose(elements, false, false);
            }
            elements = EMPTY_DISPOSED_CONTENT;
        }
        dependencyAdapter.dispose();
        dependencyAdapter = VoidContentDependencyAdapter.INSTANCE;
        parent = null;
        nullify();
    }
}
