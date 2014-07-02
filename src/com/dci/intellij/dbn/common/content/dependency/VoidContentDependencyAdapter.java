package com.dci.intellij.dbn.common.content.dependency;

import com.dci.intellij.dbn.common.content.DynamicContent;

public class VoidContentDependencyAdapter implements ContentDependencyAdapter{
    public static final VoidContentDependencyAdapter INSTANCE = new VoidContentDependencyAdapter();

    private VoidContentDependencyAdapter() {

    }

    @Override
    public boolean shouldLoad() {
        return false;
    }

    @Override
    public boolean shouldLoadIfDirty() {
        return false;
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public void beforeLoad() {

    }

    @Override
    public void afterLoad() {

    }

    @Override
    public void beforeReload(DynamicContent dynamicContent) {

    }

    @Override
    public void afterReload(DynamicContent dynamicContent) {

    }

    @Override
    public boolean canLoadFast() {
        return true;
    }

    @Override
    public boolean isSubContent() {
        return false;
    }

    @Override
    public void dispose() {

    }
}
