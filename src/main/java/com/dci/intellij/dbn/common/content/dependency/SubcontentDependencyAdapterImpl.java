package com.dci.intellij.dbn.common.content.dependency;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.connection.DatabaseEntity;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterfaceQueue;
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
    public boolean canUseAlternativeLoader() {
        DynamicContent sourceContent = getSourceContent();
        DatabaseInterfaceQueue interfaceQueue = sourceContent.getConnection().getInterfaceQueue();
        int maxActiveTasks = interfaceQueue.maxActiveTasks();
        int count = interfaceQueue.size() + interfaceQueue.counters().active();

        //ThreadInfo thread = ThreadMonitor.current();
        if (count > maxActiveTasks /* || thread.is(ThreadProperty.CODE_ANNOTATING) || ThreadMonitor.getProcessCount(ThreadProperty.PROGRESS) > 20*/ ) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean canLoad() {
        return getSourceContent().isLoaded();
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
        if (!force) return;

        DynamicContent sourceContent = getSourceContent();
        sourceContent.refresh();
    }

    @Override
    public void afterLoad() {
        contentDependency.updateSignature();
    }

    @Override
    public boolean canLoadFast() {
        DynamicContent sourceContent = getSourceContent();
        return sourceContent.isLoaded() && !sourceContent.isDirty();
    }

    @Override
    public boolean canLoadInBackground() {
        DynamicContent sourceContent = getSourceContent();
        if (sourceContent.isLoadingInBackground()) return false;

        ContentDependencyAdapter sourceDependencyAdapter = sourceContent.getDependencyAdapter();
        if (!sourceDependencyAdapter.canLoadInBackground()) return false;

        return true;
    }

    @Override
    public void dispose() {
        Disposer.dispose(contentDependency);
        contentDependency = VoidContentDependency.INSTANCE;
        super.dispose();
    }


}
