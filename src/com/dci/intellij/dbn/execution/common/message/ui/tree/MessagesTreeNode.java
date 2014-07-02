package com.dci.intellij.dbn.execution.common.message.ui.tree;

import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.tree.TreeNode;

interface MessagesTreeNode extends TreeNode {
    void dispose();
    MessagesTreeModel getTreeModel();
    VirtualFile getVirtualFile();
}
