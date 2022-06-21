package com.dci.intellij.dbn.common.content.loader;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentElement;
import com.dci.intellij.dbn.common.content.DynamicContentProperty;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.content.dependency.ContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.SubcontentDependencyAdapter;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class DynamicSubcontentCustomLoader<
                T extends DynamicContentElement,
                M extends DBObjectMetadata>
        extends DynamicContentLoaderImpl<T, M>
        implements DynamicContentLoader<T, M> {

    public DynamicSubcontentCustomLoader(
            @Nullable DynamicContentType parentContentType,
            @NotNull DynamicContentType contentType) {

        super(parentContentType, contentType, true);
    }

    protected abstract T resolveElement(DynamicContent<T> dynamicContent, DynamicContentElement sourceElement);

    @Override
    public void loadContent(DynamicContent<T> content, boolean forceReload) throws SQLException {
        List<T> list = null;
        ContentDependencyAdapter adapter = content.getDependencyAdapter();
        if (adapter instanceof SubcontentDependencyAdapter) {
            SubcontentDependencyAdapter dependencyAdapter = (SubcontentDependencyAdapter) adapter;
            List elements = dependencyAdapter.getSourceContent().getAllElements();
            for (Object object : elements) {
                content.checkDisposed();
                DynamicContentElement sourceElement = (DynamicContentElement) object;
                T element = resolveElement(content, sourceElement);
                if (element != null) {
                    content.checkDisposed();
                    if (list == null) {
                        list = content.isMutable() ?
                                CollectionUtil.createConcurrentList() :
                                new ArrayList<T>();
                    }
                    list.add(element);
                }
            }
        }

        content.setElements(list);
        content.set(DynamicContentProperty.MASTER, false);
    }
}
