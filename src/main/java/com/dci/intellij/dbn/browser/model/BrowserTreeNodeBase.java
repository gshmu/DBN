package com.dci.intellij.dbn.browser.model;

import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.TreeNode;
import java.util.Collections;
import java.util.Enumeration;

public abstract class BrowserTreeNodeBase extends StatefulDisposable.Base implements BrowserTreeNode{

    @Nullable
    @Override
    public String getLocationString() {
        return null;
    }

    @Override
    public Enumeration<? extends BrowserTreeNode> children() {
        return Collections.enumeration(getChildren());
    }

    @Override
    public int getIndex(TreeNode child) {
        return getIndex((BrowserTreeNode) child);
    }

    @Override
    public boolean getAllowsChildren() {
        return !isLeaf();
    }
}
