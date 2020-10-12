package com.dci.intellij.dbn.browser.ui;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.component.DBNComponent;
import org.jetbrains.annotations.Nullable;

public abstract class DatabaseBrowserForm extends DBNFormImpl {
    DatabaseBrowserForm(DBNComponent parent) {
        super(parent);
    }

    @Nullable
    public abstract DatabaseBrowserTree getBrowserTree();

    public abstract void selectElement(BrowserTreeNode treeNode, boolean focus, boolean scroll);

    public abstract void rebuildTree();
}
