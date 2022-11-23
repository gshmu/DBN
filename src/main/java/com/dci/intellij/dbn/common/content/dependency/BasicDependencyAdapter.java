package com.dci.intellij.dbn.common.content.dependency;

public class BasicDependencyAdapter implements ContentDependencyAdapter {
    public static BasicDependencyAdapter INSTANCE = new BasicDependencyAdapter();

    @Override
    public void refreshSources() {

    }

    @Override
    public boolean canLoadFast() {
        return false;
    }

    @Override
    public boolean isSubContent() {
        return false;
    }

    @Override
    public void dispose() {
    }
}
