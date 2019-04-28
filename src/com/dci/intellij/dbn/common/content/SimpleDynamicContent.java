package com.dci.intellij.dbn.common.content;

import com.dci.intellij.dbn.common.content.dependency.BasicDependencyAdapter;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadata;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SimpleDynamicContent<T extends DynamicContentElement, M extends DBObjectMetadata> extends DynamicContentImpl<T> {
    private DynamicContentLoader<T, M> loader;

    public SimpleDynamicContent(@NotNull GenericDatabaseElement parent, DynamicContentLoader<T, M> loader, DynamicContentStatus ... status) {
        super(parent, BasicDependencyAdapter.INSTANCE, status);
        this.loader = loader;
    }

    @Override
    public DynamicContentLoader<T, M> getLoader() {
        return loader;
    }

    @Nullable
    @Override
    public Filter<T> getFilter() {
        return null;
    }

    @Override
    public void notifyChangeListeners() {

    }

    @Override
    public Project getProject() {
        return null;
    }

    @Override
    public DynamicContentType getContentType() {
        return null;
    }

    @Override
    public String getContentDescription() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }
}
