package com.dci.intellij.dbn.common.ui.panel;

import com.dci.intellij.dbn.common.dispose.Nullifier;
import com.intellij.ui.components.JBPanel;
import lombok.Getter;

public abstract class DBNPanelImpl extends JBPanel implements DBNPanel{
    @Getter
    private boolean disposed;

    @Override
    public final void dispose() {
        if (!disposed) {
            disposed = true;
            disposeInner();
            Nullifier.nullify(this);
        }
    }

    protected abstract void disposeInner();
}
