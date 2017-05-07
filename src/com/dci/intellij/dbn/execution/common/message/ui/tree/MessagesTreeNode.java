package com.dci.intellij.dbn.execution.common.message.ui.tree;

import javax.swing.tree.TreeNode;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.intellij.openapi.vfs.VirtualFile;

interface MessagesTreeNode extends TreeNode, Disposable {
    MessagesTreeModel getTreeModel();
    VirtualFile getVirtualFile();
}
