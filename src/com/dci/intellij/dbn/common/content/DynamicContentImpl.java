package com.dci.intellij.dbn.common.content;

import com.dci.intellij.dbn.common.content.dependency.ContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.VoidContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.SafeDisposer;
import com.dci.intellij.dbn.common.filter.FilterDelegate;
import com.dci.intellij.dbn.common.list.FilteredList;
import com.dci.intellij.dbn.common.notification.NotificationGroup;
import com.dci.intellij.dbn.common.notification.NotificationSupport;
import com.dci.intellij.dbn.common.property.DisposablePropertyHolder;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.thread.ThreadMonitor;
import com.dci.intellij.dbn.common.thread.ThreadProperty;
import com.dci.intellij.dbn.common.util.Compactables;
import com.dci.intellij.dbn.common.util.Lists;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.common.util.Unsafe;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.DatabaseEntity;
import com.intellij.openapi.progress.ProcessCanceledException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.dci.intellij.dbn.common.content.DynamicContentStatus.*;
import static com.dci.intellij.dbn.common.util.Unsafe.cast;

@Slf4j
public abstract class DynamicContentImpl<T extends DynamicContentElement>
        extends DisposablePropertyHolder<DynamicContentStatus>
        implements DynamicContent<T>,
                   NotificationSupport {

    protected static final List<?> EMPTY_CONTENT = Collections.unmodifiableList(new ArrayList<>(0));
    protected static final List<?> EMPTY_DISPOSED_CONTENT = Collections.unmodifiableList(new ArrayList<>(0));
    protected static final List<?> EMPTY_UNTOUCHED_CONTENT = Collections.unmodifiableList(new ArrayList<>(0));

    private byte signature = 0;

    private DatabaseEntity parent;
    private ContentDependencyAdapter dependencyAdapter;

    protected List<T> elements = cast(EMPTY_UNTOUCHED_CONTENT);

    protected DynamicContentImpl(
            @NotNull DatabaseEntity parent,
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
    protected DynamicContentStatus[] properties() {
        return DynamicContentStatus.values();
    }

    @Override
    @NotNull
    public <E extends DatabaseEntity> E getParentEntity() {
        return Unsafe.cast(Failsafe.nn(parent));
    }

    @NotNull
    public ConnectionId getConnectionId() {
        return getParentEntity().getConnectionId();
    }

    @Override
    @NotNull
    public ConnectionHandler getConnection() {
        return Failsafe.nn(getParentEntity().getConnection());
    }

    @Override
    public abstract DynamicContentLoader<T, ?> getLoader();

    @Override
    public ContentDependencyAdapter getDependencyAdapter() {
        return dependencyAdapter;
    }

    @Override
    public byte getSignature() {
        return signature;
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

        ConnectionHandler connectionHandler = this.getConnection();
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
                updateSignature();
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
                updateSignature();
            }

            for (T element : elements) {
                checkDisposed();
                element.refresh();
            }
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
                    for (T e : elements) {
                        checkDisposed();
                        e.refresh();
                    }
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
            ConnectionHandler connectionHandler = this.getConnection();
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
            if (force) {
                for (T element : elements) {
                    element.refresh();
                }
            }

        } catch (ProcessCanceledException e) {
            throw e;

        } catch (SQLFeatureNotSupportedException e) {
            // unsupported feature: log in notification area
            elements = cast(EMPTY_CONTENT);
            set(LOADED, true);
            set(ERROR, true);
            sendWarningNotification(
                    NotificationGroup.METADATA,
                    "Failed to load {0}. Feature not supported: {1}",
                    getContentDescription(), e);

        } catch (SQLException e) {
            // connectivity / timeout exceptions: mark content dirty (no logging)
            elements = cast(EMPTY_CONTENT);
            set(DIRTY, true);

        } catch (Throwable e) {
            // any other exception: log error
            log.error("Failed to load content", e);
            elements = cast(EMPTY_CONTENT);
            set(DIRTY, true);
        }

        checkDisposed();
        dependencyAdapter.afterLoad();
    }

    @Override
    public void updateSignature() {
        signature++;
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
        if (isDisposed() || elements == null || elements.size() == 0) {
            elements = cast(EMPTY_CONTENT);
        } else {
            sortElements(elements);
            Compactables.compact(elements);
        }
        List<T> oldElements = this.elements;
        this.elements = FilteredList.stateful((FilterDelegate<T>) () -> getFilter(), elements);
        if (oldElements.size() != 0 || elements.size() != 0 ){
            notifyChangeListeners();
        }
        if (is(MASTER)) {
            SafeDisposer.disposeCollection(oldElements, false, true);
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

    public List<T> getElementsNoLoad() {
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
            return Lists.first(elements, element -> matchElement(element, name, overload));
        }
        return null;
    }

    private boolean matchElement(T element, String name, short overload) {
        return (overload == 0 || overload == element.getOverload()) &&
                Strings.equalsIgnoreCase(element.getName(), name);
    }

    @Override
    @Nullable
    public List<T> getElements(String name) {
        return Lists.filter(getAllElements(), element -> Strings.equalsIgnoreCase(element.getName(), name));
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
                SafeDisposer.disposeCollection(elements, true, true);
            }
            elements = cast(EMPTY_DISPOSED_CONTENT);
        }
        dependencyAdapter.dispose();
        dependencyAdapter = VoidContentDependencyAdapter.INSTANCE;
        parent = null;
        nullify();
    }

    @Override
    protected DynamicContentStatus getDisposedProperty() {
        return DISPOSED;
    }
}
