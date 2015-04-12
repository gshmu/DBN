package com.dci.intellij.dbn.browser.model;

import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.intellij.openapi.project.Project;

public class SimpleBrowserTreeModel extends BrowserTreeModel {
    public static final SimpleBrowserTreeModel EMPTY_MODEL = new SimpleBrowserTreeModel();
    private SimpleBrowserTreeModel() {
        this(null, null);
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
