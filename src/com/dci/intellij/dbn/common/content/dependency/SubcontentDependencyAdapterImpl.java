package com.dci.intellij.dbn.common.content.dependency;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.DatabaseEntity;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;

class SubcontentDependencyAdapterImpl extends BasicDependencyAdapter implements SubcontentDependencyAdapter {
    private ContentDependency contentDependency;

    SubcontentDependencyAdapterImpl(@NotNull DatabaseEntity sourceContentOwner, @NotNull DynamicContentType sourceContentType) {
        contentDependency = new LinkedContentDependency(sourceContentOwner, sourceContentType);
    }


    @Override
    @NotNull
    public DynamicContent getSourceContent() {
        return contentDependency.getSourceContent();
    }

    @Override
    public boolean isSourceContentReady() {
        DynamicContent sourceContent = getSourceContent();
        return sourceContent.isLoaded() && !sourceContent.isLoading() && !sourceContent.isDirty();
    }

    @Override
    public boolean canLoad(ConnectionHandler connection) {
        return canConnect(connection) && getSourceContent().isLoaded();
    }

    @Override
    public boolean isDependencyDirty() {
        return contentDependency.isDirty();
    }

    @Override
    public void refreshSources() {
        DynamicContent sourceContent = getSourceContent();
        sourceContent.refresh();
    }

    @Override
    public void beforeLoad(boolean force) {
        if (force) {
            DynamicContent sourceContent = getSourceContent();
            sourceContent.refresh();
        }
    }

    @Override
    public void afterLoad() {
        contentDependency.updateSignature();
    }

    @Override
    public boolean canLoadFast() {
        return getSourceContent().isLoaded();
    }

    @Override
    public boolean isSubContent() {
        return true;
    }

    @Override
    public void dispose() {
        Disposer.dispose(contentDependency);
        contentDependency = VoidContentDependency.INSTANCE;
        super.dispose();
    }


}
