package com.dci.intellij.dbn.execution.common.message.ui.tree;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.tree.TreeNode;

interface MessagesTreeNode extends TreeNode, Disposable {
    MessagesTreeModel getTreeModel();
    VirtualFile getVirtualFile();
}
