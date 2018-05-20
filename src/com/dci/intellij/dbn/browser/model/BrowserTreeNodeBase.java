package com.dci.intellij.dbn.browser.model;

import com.dci.intellij.dbn.common.dispose.DisposableBase;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.TreeNode;
import java.util.Collections;
import java.util.Enumeration;

public abstract class BrowserTreeNodeBase extends DisposableBase implements BrowserTreeNode{

    @Nullable
    @Override
    public String getLocationString() {
        return null;
    }

    @Override
    public Enumeration children() {
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
