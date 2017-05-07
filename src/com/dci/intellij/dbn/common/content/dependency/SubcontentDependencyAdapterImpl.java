package com.dci.intellij.dbn.common.content.dependency;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.intellij.openapi.util.Disposer;

public class SubcontentDependencyAdapterImpl extends BasicDependencyAdapter implements SubcontentDependencyAdapter {
    private ContentDependency contentDependency;

    public SubcontentDependencyAdapterImpl(@NotNull GenericDatabaseElement sourceContentOwner, @NotNull DynamicContentType sourceContentType) {
        contentDependency = new LinkedContentDependency(sourceContentOwner, sourceContentType);
    }


    public DynamicContent getSourceContent() {
        return contentDependency.getSourceContent();
    }

    @Override
    public boolean canLoad(ConnectionHandler connectionHandler) {
        return canConnect(connectionHandler) && contentDependency.getSourceContent().isLoaded();
    }

    @Override
    public void markSourcesDirty() {
        contentDependency.markSourcesDirty();
    }

    public boolean isDirty() {
        return contentDependency.isDirty();
    }

    @Override
    public void beforeLoad() {
        //contentDependency.getSourceContent().loadInBackground();
    }

    @Override
    public void afterLoad() {
        contentDependency.reset();
    }

    public void beforeReload(DynamicContent dynamicContent) {
/*        DynamicContent sourceContent = contentDependency.getSourceContent();
        sourceContent.getDependencyAdapter().beforeReload(sourceContent);
        sourceContent.removeElements(dynamicContent.getElements());*/
    }

    public void afterReload(DynamicContent dynamicContent) {
/*        DynamicContent sourceContent = contentDependency.getSourceContent();
        if (sourceContent.getClass().isAssignableFrom(dynamicContent.getClass())) {
            sourceContent.addElements(dynamicContent.getElements());
            sourceContent.getDependencyAdapter().afterReload(sourceContent);
            sourceContent.updateChangeTimestamp();
            contentDependency.reset();
        }*/
    }

    @Override
    public boolean canLoadFast() {
        return getSourceContent().isLoaded();
    }

    @Override
    public boolean isSubContent() {
        return true;
    }

    public void dispose() {
        Disposer.dispose(contentDependency);
        contentDependency = VoidContentDependency.INSTANCE;
        super.dispose();
    }


}
