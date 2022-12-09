package com.dci.intellij.dbn.common.content;

import com.dci.intellij.dbn.common.content.dependency.ContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.dispose.UnlistedDisposable;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.property.PropertyHolder;
import com.dci.intellij.dbn.connection.DatabaseEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface DynamicContent<T extends DynamicContentElement> extends
        DatabaseEntity,
        StatefulDisposable,
        UnlistedDisposable,
        PropertyHolder<DynamicContentProperty>{

    /**
     * Triggering the actual load of the content
     */
    void load();

    /**
     * Rebuilds the content. This method is called when reloading the content
     * is triggered deliberately by the user directly or by a ddl change.
     */
    void reload();

    /**
     * Soft reload. Mark sources dirty
     */
    void refresh();

    void loadInBackground();

    /**
     * The signature of the last change on the content (incrementing byte).
     */
    byte getSignature();

    /**
     * A load attempt has been made already
     */
    boolean isLoaded();

    boolean isLoading();

    boolean isLoadingInBackground();

    /**
     * The content has been loaded but with errors (e.g. because of database connectivity problems)
     */
    boolean isDirty();

    default boolean isSubContent() {
        return getDependencyAdapter().isSubContent();
    }

    default boolean canLoad() {
        return getDependencyAdapter().canLoad();
    }

    default boolean canLoadFast() {
        return getDependencyAdapter().canLoadFast();
    }

    default boolean canLoadInBackground() {
        return getDependencyAdapter().canLoadInBackground();
    }

    default boolean isDependencyDirty() {
        return getDependencyAdapter().isDependencyDirty();
    }

    boolean isEmpty();

    void markDirty();

    DynamicContentType getContentType();

    String getContentDescription();

    List<T> getElements();

    List<T> getElements(String name);

    List<T> getAllElements();

    @Nullable
    default Filter<T> getFilter() {
        return null;
    }


    T getElement(String name, short overload);

    void setElements(@Nullable List<T> elements);

    int size();

    DynamicContentLoader getLoader();

    ContentDependencyAdapter getDependencyAdapter();
}
