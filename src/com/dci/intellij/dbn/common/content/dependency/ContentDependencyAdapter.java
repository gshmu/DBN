package com.dci.intellij.dbn.common.content.dependency;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.intellij.openapi.Disposable;

public interface ContentDependencyAdapter extends Disposable {

    /**
     * This method is called every time the content is being queried.
     * Typical implementation would be to check if dependencies have changed.
     * @deprecated
     */
    boolean shouldLoad();

    /**
     * This method is typically called when the dynamic content is dirty and
     * the system tries to reload it.
     * e.g. one basic condition for reloading dirty content is valid connectivity
     */
    boolean shouldLoadIfDirty();

    boolean isDirty();

    /**
     * This operation is triggered before loading the dynamic content is started.
     * It can be implemented by the adapters to load non-weak dependencies for example.
     */
    void beforeLoad();

    /**
     * This operation is triggered after the loading of the dynamic content.
     */
    void afterLoad();

    void beforeReload(DynamicContent dynamicContent);

    void afterReload(DynamicContent dynamicContent);

    boolean isSubContent();

    boolean canLoadFast();
}
