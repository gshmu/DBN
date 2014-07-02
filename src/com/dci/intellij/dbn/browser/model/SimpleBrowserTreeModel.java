package com.dci.intellij.dbn.browser.model;

import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.intellij.openapi.project.Project;

import java.util.List;

public class SimpleBrowserTreeModel extends BrowserTreeModel {
    public SimpleBrowserTreeModel(Project project, List<ConnectionBundle> connectionBundles) {
        super(new SimpleBrowserTreeRoot(project, connectionBundles));
    }

    @Override
    public boolean contains(BrowserTreeNode node) {
        return true;
    }

    public void dispose() {
        super.dispose();
    }
}
