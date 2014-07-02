package com.dci.intellij.dbn.common.content.loader;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentElement;

import java.sql.SQLException;

public interface DynamicContentLoader<T extends DynamicContentElement> {
    SQLException DBN_INTERRUPTED_EXCEPTION = new SQLException("DBN_INTERRUPTED_EXCEPTION");
    void loadContent(DynamicContent<T> dynamicContent, boolean forceReload) throws DynamicContentLoadException, InterruptedException;
    void reloadContent(DynamicContent<T> dynamicContent) throws DynamicContentLoadException, InterruptedException;
}
