package com.dci.intellij.dbn.common.content.loader;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentElement;
import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadata;

import java.sql.SQLException;

public interface DynamicContentLoader<T extends DynamicContentElement, M extends DBObjectMetadata> {
    void loadContent(DynamicContent<T> dynamicContent, boolean forceReload) throws SQLException;

    DynamicContentLoader VOID_CONTENT_LOADER = (dynamicContent, forceReload) -> {};
}
