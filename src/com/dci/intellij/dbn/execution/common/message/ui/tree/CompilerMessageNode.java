package com.dci.intellij.dbn.execution.common.message.ui.tree;

import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.execution.common.message.ConsoleMessage;
import com.dci.intellij.dbn.execution.compiler.CompilerMessage;
import com.dci.intellij.dbn.vfs.file.DBContentVirtualFile;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreeNode;
import java.util.Enumeration;

public class CompilerMessageNode extends DisposableBase implements MessageTreeNode {
    private CompilerMessage compilerMessage;
    private CompilerMessagesObjectNode parent;

    public CompilerMessageNode(CompilerMessagesObjectNode parent, CompilerMessage compilerMessage) {
        this.parent = parent;
        this.compilerMessage = compilerMessage;

        Disposer.register(this, compilerMessage);
    }

    public CompilerMessage getCompilerMessage() {
        return Failsafe.get(compilerMessage);
    }

    @Override
    public DBContentVirtualFile getVirtualFile() {
        return getCompilerMessage().getContentFile();
    }

    @Override
    public MessagesTreeModel getTreeModel() {
        return getParent().getTreeModel();
    }

    /*********************************************************
     *                        TreeNode                       *
     *********************************************************/
    @Override
    public TreeNode getChildAt(int childIndex) {
        return null;
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public CompilerMessagesObjectNode getParent() {
        return Failsafe.get(parent);
    }

    @Override
    public int getIndex(TreeNode node) {
        return -1;
    }

    @Override
    public boolean getAllowsChildren() {
        return false;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
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

    @Override
    public void dispose() {
        super.dispose();
        compilerMessage = null;
        parent = null;
    }
}