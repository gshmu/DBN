package com.dci.intellij.dbn.browser.ui;

import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.dispose.DisposableProjectComponent;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;

public abstract class DatabaseBrowserForm extends DBNFormImpl<DisposableProjectComponent> {
    protected DatabaseBrowserForm(DisposableProjectComponent parentComponent) {
        super(parentComponent);
    }

    @Nullable
    public abstract DatabaseBrowserTree getBrowserTree();

    public abstract void selectElement(BrowserTreeNode treeNode, boolean focus, boolean scroll);

    public abstract void rebuildTree();

    @Override
    public void dispose() {
        super.dispose();
    }
}
