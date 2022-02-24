package com.dci.intellij.dbn.common.content.dependency;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.openapi.Disposable;

public interface ContentDependencyAdapter extends Disposable {
    boolean canConnect(ConnectionHandler connection);

    /**
     * This method is typically called when the dynamic content is dirty and
     * the system tries to reload it.
     * e.g. one basic condition for reloading dirty content is valid connectivity
     */
    boolean canLoad(ConnectionHandler connection);

    boolean areDependenciesDirty();

    void refreshSources();

    /**
     * This operation is triggered before loading the dynamic content is started.
     * It can be implemented by the adapters to load non-weak dependencies for example.
     * @param force load flavor
     */
    default void beforeLoad(boolean force) {};

    /**
     * This operation is triggered after the loading of the dynamic content.
     */
    default void afterLoad() {};

    boolean isSubContent();

    boolean canLoadFast();
}
