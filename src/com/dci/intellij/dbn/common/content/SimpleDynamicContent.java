package com.dci.intellij.dbn.common.content;

import com.dci.intellij.dbn.common.content.dependency.BasicDependencyAdapter;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SimpleDynamicContent<T extends DynamicContentElement> extends DynamicContentImpl<T> {
    private DynamicContentLoader<T> loader;

    public SimpleDynamicContent(@NotNull GenericDatabaseElement parent, DynamicContentLoader<T> loader, DynamicContentStatus ... status) {
        super(parent, BasicDependencyAdapter.INSTANCE, status);
        this.loader = loader;
    }

    @Override
    public DynamicContentLoader<T> getLoader() {
        return loader;
    }

    @Nullable
    @Override
    public Filter<T> getFilter() {
        return null;
    }

    public void notifyChangeListeners() {

    }

    public Project getProject() {
        return null;
    }

    public String getContentDescription() {
        return null;
    }

    public String getName() {
        return null;
    }
}
