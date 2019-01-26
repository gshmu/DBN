package com.dci.intellij.dbn.common.content.loader;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentElement;

public interface DynamicContentLoader<T extends DynamicContentElement> {
    void loadContent(DynamicContent<T> dynamicContent, boolean forceReload) throws DynamicContentLoadException, InterruptedException;
    void reloadContent(DynamicContent<T> dynamicContent) throws DynamicContentLoadException, InterruptedException;

    DynamicContentLoader VOID_CONTENT_LOADER = new DynamicContentLoader() {
        @Override
        public void loadContent(DynamicContent dynamicContent, boolean forceReload) {}
        @Override
        public void reloadContent(DynamicContent dynamicContent) {}
    };
}
