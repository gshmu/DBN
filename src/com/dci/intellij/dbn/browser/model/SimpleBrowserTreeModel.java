package com.dci.intellij.dbn.browser.model;

import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.intellij.openapi.project.Project;

public class SimpleBrowserTreeModel extends BrowserTreeModel {
    public SimpleBrowserTreeModel() {
        this(FailsafeUtil.DUMMY_PROJECT, null);
    }

    public SimpleBrowserTreeModel(Project project, ConnectionBundle connectionBundle) {
        super(new SimpleBrowserTreeRoot(project, connectionBundle));
    }

    @Override
    public boolean contains(BrowserTreeNode node) {
        return true;
    }

    public void dispose() {
        super.dispose();
    }
}
