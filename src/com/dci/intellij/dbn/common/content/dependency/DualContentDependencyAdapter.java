package com.dci.intellij.dbn.common.content.dependency;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import org.jetbrains.annotations.NotNull;

public class DualContentDependencyAdapter extends BasicDependencyAdapter implements ContentDependencyAdapter {
    private ContentDependency first;
    private ContentDependency second;

    private DualContentDependencyAdapter(DynamicContent firstContent, DynamicContent secondContent) {
        first = dependency(firstContent);

        if (firstContent == secondContent) {
            // dual dependencies may rely on same content
            second = first;
        } else {
            second = dependency(secondContent);
        }
    }

    public static DualContentDependencyAdapter create(DynamicContent firstContent, DynamicContent secondContent) {
        return new DualContentDependencyAdapter(firstContent, secondContent);
    }

    @Override
    public boolean canLoad(ConnectionHandler connection) {
        if (canConnect(connection)) {
            return
                content(first).isLoaded() &&
                content(second).isLoaded();
        }
        return false;
    }

    @Override
    public boolean areDependenciesDirty() {
        return
            content(first).isDirty() ||
            content(second).isDirty();
    }

    @Override
    public void refreshSources() {
        content(first).refresh();
        content(second).refresh();
        super.refreshSources();
    }

    @Override
    public boolean canLoadFast() {
        return
            content(first).isLoaded() &&
            content(second).isLoaded();
    }

    @Override
    public void beforeLoad(boolean force) {
        if (force) {
            content(first).refresh();
            content(second).refresh();
        }
    }

    @Override
    public void afterLoad() {
        first.updateSignature();
        second.updateSignature();
    }

    private static DynamicContent content(ContentDependency dependency) {
        return dependency.getSourceContent();
    }

    @NotNull
    private static ContentDependency dependency(DynamicContent content) {
        return content == null ? VoidContentDependency.INSTANCE : new BasicContentDependency(content);
    }

    @Override
    public void dispose() {
        first = VoidContentDependency.INSTANCE;
        second = VoidContentDependency.INSTANCE;
        super.dispose();
    }
}
