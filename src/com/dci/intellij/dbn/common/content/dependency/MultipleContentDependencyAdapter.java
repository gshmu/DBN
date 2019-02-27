package com.dci.intellij.dbn.common.content.dependency;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;

import java.util.ArrayList;
import java.util.List;

public class MultipleContentDependencyAdapter extends BasicDependencyAdapter implements ContentDependencyAdapter {
    private List<ContentDependency> dependencies;

    public MultipleContentDependencyAdapter(DynamicContent... sourceContents) {
        for (DynamicContent sourceContent : sourceContents) {
            if (sourceContent != null) {
                if (dependencies == null) dependencies = new ArrayList<>();
                dependencies.add(new BasicContentDependency(sourceContent));
            }
        }
    }

    @Override
    public boolean canLoad(ConnectionHandler connectionHandler) {
        if (dependencies != null && canConnect(connectionHandler)) {
            for (ContentDependency dependency : dependencies) {
                if (!dependency.getSourceContent().isLoaded()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean areSourcesDirty() {
        if (dependencies != null) {
            for (ContentDependency dependency : dependencies) {
                if (dependency.isDirty()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void refreshSources() {
        if (dependencies != null) {
            for (ContentDependency dependency : dependencies) {
                dependency.getSourceContent().refresh();
            }
        }

        super.refreshSources();
    }

    @Override
    public boolean canLoadFast() {
        if (dependencies != null) {
            for (ContentDependency dependency : dependencies) {
                if (!dependency.getSourceContent().isLoaded()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void beforeLoad(boolean force) {
        if (force && dependencies != null) {
            for (ContentDependency dependency : dependencies) {
                DynamicContent sourceContent = dependency.getSourceContent();
                sourceContent.refresh();
            }
        }
    }

    @Override
    public void afterLoad() {
        if (dependencies != null) {
            for (ContentDependency dependency : dependencies) {
                dependency.reset();
            }
        }
    }

    @Override
    public void dispose() {
        DisposerUtil.dispose(dependencies);
        CollectionUtil.clear(dependencies);
        dependencies = null;
        super.dispose();
    }
}
