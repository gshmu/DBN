package com.dci.intellij.dbn.execution.common.message.ui.tree;

import javax.swing.tree.TreeNode;
import java.util.Enumeration;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.execution.common.message.ConsoleMessage;
import com.dci.intellij.dbn.execution.compiler.CompilerMessage;
import com.dci.intellij.dbn.vfs.DBContentVirtualFile;
import com.intellij.openapi.util.Disposer;

public class CompilerMessageNode extends DisposableBase implements MessageTreeNode {
    private CompilerMessage compilerMessage;
    private CompilerMessagesObjectNode parent;

    public CompilerMessageNode(CompilerMessagesObjectNode parent, CompilerMessage compilerMessage) {
        this.parent = parent;
        this.compilerMessage = compilerMessage;

        Disposer.register(this, compilerMessage);
    }

    public CompilerMessage getCompilerMessage() {
        return FailsafeUtil.get(compilerMessage);
    }

    public DBContentVirtualFile getVirtualFile() {
        return getCompilerMessage().getContentFile();
    }

    public MessagesTreeModel getTreeModel() {
        return getParent().getTreeModel();
    }

    /*********************************************************
     *                        TreeNode                       *
     *********************************************************/
    public TreeNode getChildAt(int childIndex) {
        return null;
    }

    public int getChildCount() {
        return 0;
    }

    public CompilerMessagesObjectNode getParent() {
        return FailsafeUtil.get(parent);
    }

    public int getIndex(TreeNode node) {
        return -1;
    }

    public boolean getAllowsChildren() {
        return false;
    }

    public boolean isLeaf() {
        return true;
    }

    public Enumeration children() {
        return null;
    }

    @Override
    public String toString() {
        CompilerMessage compilerMessage = getCompilerMessage();
        return "[" + compilerMessage.getType() + "] " + compilerMessage.getText();
    }

    @NotNull
    @Override
    public ConsoleMessage getMessage() {
        return getCompilerMessage();
    }

    public void dispose() {
        super.dispose();
        compilerMessage = null;
        parent = null;
    }
}