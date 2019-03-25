package com.dci.intellij.dbn.execution.common.message.ui.tree;

import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.Nullifiable;

@Nullifiable
public abstract class MessagesTreeNodeBase<P extends MessagesTreeNode, C extends MessagesTreeNode>
        extends DisposableBase
        implements MessagesTreeNode<P, C>{

    private P parent;

    MessagesTreeNodeBase(P parent) {
        this.parent = parent;
    }

    @Override
    public P getParent() {
        return parent;
    }
}
