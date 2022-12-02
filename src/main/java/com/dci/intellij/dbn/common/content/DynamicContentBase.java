package com.dci.intellij.dbn.common.content;

import com.dci.intellij.dbn.common.collections.CompactArrayList;
import com.dci.intellij.dbn.common.content.dependency.ContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.VoidContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.filter.FilterDelegate;
import com.dci.intellij.dbn.common.list.FilteredList;
import com.dci.intellij.dbn.common.notification.NotificationGroup;
import com.dci.intellij.dbn.common.notification.NotificationSupport;
import com.dci.intellij.dbn.common.property.DisposablePropertyHolder;
import com.dci.intellij.dbn.common.thread.Background;
import com.dci.intellij.dbn.common.thread.Synchronized;
import com.dci.intellij.dbn.common.thread.ThreadMonitor;
import com.dci.intellij.dbn.common.util.Lists;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.DatabaseEntity;
import com.intellij.openapi.progress.ProcessCanceledException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.dci.intellij.dbn.common.content.DynamicContentProperty.*;
import static com.dci.intellij.dbn.common.list.FilteredList.unwrap;
import static com.dci.intellij.dbn.common.util.Unsafe.cast;

@Slf4j
public abstract class DynamicContentBase<T extends DynamicContentElement>
        extends DisposablePropertyHolder<DynamicContentProperty>
        implements DynamicContent<T>,
                   NotificationSupport {

    protected static final List<?> EMPTY_CONTENT = Collections.unmodifiableList(new ArrayList<>(0));
    protected static final List<?> EMPTY_DISPOSED_CONTENT = Collections.unmodifiableList(new ArrayList<>(0));
    protected static final List<?> EMPTY_UNTOUCHED_CONTENT = Collections.unmodifiableList(new ArrayList<>(0));

    private ContentDependencyAdapter dependencyAdapter;
    private DatabaseEntity parent;
    private byte signature = 0;

    protected List<T> elements = cast(EMPTY_UNTOUCHED_CONTENT);

    protected DynamicContentBase(
            @NotNull DatabaseEntity parent,
            ContentDependencyAdapter dependencyAdapter,
            DynamicContentProperty... properties) {

        this.parent = parent;
        this.dependencyAdapter = dependencyAdapter;
        if (properties != null && properties.length > 0) {
            for (DynamicContentProperty status : properties) {
                set(status, true);
            }
        }
    }

    @Override
    protected DynamicContentProperty[] properties() {
        return DynamicContentProperty.VALUES;
    }

    @Override
    @NotNull
    public <E extends DatabaseEntity> E getParentEntity() {
        return cast(Failsafe.nn(parent));
    }

    @NotNull
    public ConnectionId getConnectionId() {
        return getParentEntity().getConnectionId();
    }

    @Override
    @NotNull
    public ConnectionHandler getConnection() {
        return getParentEntity().getConnection();
    }

    private boolean canConnect() {
        ConnectionHandler connection = getConnection();
        return ConnectionHandler.canConnect(connection);
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

    public boolean isLoadingInBackground() {
        return is(LOADING_IN_BACKGROUND);
    }

    @Override
    public boolean isDirty() {
        return is(DIRTY) || isDependencyDirty();
    }



    @Override
    public void markDirty() {
        set(DIRTY, true);
    }

    private boolean shouldLoad() {
        if (!isLoaded()) {
            if (isDisposed()) return false;
            if (isLoading()) return false;
            if (!canConnect()) return false;
            return true;
        }

        if (isDirty()) {
            if (isDisposed()) return false;
            if (isLoading()) return false;
            if (!canLoad()) return false;
            if (!canConnect()) return false;
            return true;
        }

        return false;
    }

    private boolean shouldLoadInBackground() {
        if (shouldLoad()) {
            if (isLoadingInBackground()) return false;
            if (!canLoadInBackground()) return false;
            return true;
        }

        return false;
    }

    private boolean shouldReload() {
        // only allow refresh / reload if already loaded
        if (isLoaded()) {
            if (isDisposed()) return false;
            if (isLoading()) return false;
            if (isLoadingInBackground()) return false;

            return true;
        }

        return false;
    }

    private boolean shouldRefresh() {
        if (shouldReload()) {
            if (isDirty()) return false;

            return true;
        }

        return false;
    }

    @Override
    public void load() {
        if (shouldLoad()) {
            ensureLoaded(false);
        }
    }

    @Override
    public final void loadInBackground() {
        if (shouldLoadInBackground()) {
            set(LOADING_IN_BACKGROUND, true);
            Background.run(() -> {
                try {
                    ensureLoaded(false);
                } finally {
                    set(LOADING_IN_BACKGROUND, false);
                }
            });
        }
    }

    @Override
    public final void reload() {
        if (shouldReload()) {
            markDirty();
            ensureLoaded(true);
            refreshElements();
        }
    }

    @Override
    public void refresh() {
        if (shouldRefresh()) {
            markDirty();
            refreshSources();
            if (!is(INTERNAL)){
                refreshElements();
            }
        }
    }

    private void refreshSources() {
        dependencyAdapter.refreshSources();
    }

    private void refreshElements() {
        elements.forEach(e -> e.refresh());
    }

    /**
     * Synchronised block making sure the content is loaded before the thread is released
     */
    private void ensureLoaded(boolean force) {
        if (shouldLoad()) {
            Synchronized.on(this, () -> {
                if (shouldLoad()) {
                    set(LOADING, true);
                    try {
                        performLoad(force);
                    } finally {
                        set(LOADING, false);
                        changeSignature();
                    }
                }
            });
        }
    }

    private void performLoad(boolean force) {
        checkDisposed();
        dependencyAdapter.beforeLoad(force);
        checkDisposed();

        try {
            // mark first the dirty status since dirty dependencies may
            // become valid due to parallel background load
            set(DIRTY, false);
            DynamicContentLoader<T, ?> loader = getLoader();
            loader.loadContent(this);
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

    protected void changeSignature() {
        signature++;
    }


    /**
     * do whatever is needed after the content is loaded (e.g. refresh browser tree..)
     */
    public abstract void notifyChangeListeners();

    @Override
    public void setElements(List<T> elements) {
        conditional(CHANGING, () -> replaceElements(elements));
    }

    private void replaceElements(List<T> elements) {
        beforeUpdate();
        if (isDisposed() || elements == null || elements.size() == 0) {
            elements = cast(EMPTY_CONTENT);
        } else {
            sortElements(elements);
            elements = CompactArrayList.from(elements);
        }
        List<T> oldElements = this.elements;
        if (elements != EMPTY_CONTENT && isNot(INTERNAL) && isNot(VIRTUAL)) {
            elements = FilteredList.stateful((FilterDelegate<T>) () -> getFilter(), elements);
        }

        this.elements = elements;

        afterUpdate();
        if (oldElements.size() != 0 || elements.size() != 0 ){
            notifyChangeListeners();
        }
        if (is(MASTER)) {
            Disposer.disposeCollection(oldElements);
        }
    }

    protected void beforeUpdate() {}
    protected void afterUpdate() {}

    protected abstract void sortElements(List<T> elements);

    @Override
    public List<T> getAllElements() {
        return unwrap(getElements());
    }

    @Override
    public List<T> getElements() {
        if (isLoaded() && !isDirty()) return elements;
        if (isDisposed()) return elements;

        if (canLoadFast() ||
                ThreadMonitor.isTimeoutProcess() ||
                ThreadMonitor.isBackgroundProcess() ||
                ThreadMonitor.isProgressProcess()) {

            load();
        } else{
            loadInBackground();
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
            if (!isSubContent()) {
                Disposer.disposeCollection(elements);
            }
            elements = cast(EMPTY_DISPOSED_CONTENT);
        }
        dependencyAdapter.dispose();
        dependencyAdapter = VoidContentDependencyAdapter.INSTANCE;
        parent = null;
        nullify();
    }

    @Override
    protected DynamicContentProperty getDisposedProperty() {
        return DISPOSED;
    }
}
