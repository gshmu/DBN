package com.dci.intellij.dbn.execution.common.message.ui.tree;

import com.dci.intellij.dbn.common.dispose.StatefulDisposableBase;
import com.dci.intellij.dbn.common.ref.WeakRef;

public abstract class MessagesTreeNodeBase<P extends MessagesTreeNode, C extends MessagesTreeNode>
        extends StatefulDisposableBase
        implements MessagesTreeNode<P, C>{

    private final WeakRef<P> parent;

    MessagesTreeNodeBase(P parent) {
        this.parent = WeakRef.of(parent);
    }

    @Override
    public P getParent() {
        return WeakRef.get(parent);
    }
}
