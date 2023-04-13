package com.dci.intellij.dbn.common.ui.panel;

import com.intellij.ui.components.JBPanel;
import lombok.Getter;

public abstract class DBNPanelImpl extends JBPanel implements DBNPanel{
    @Getter
    private boolean disposed;

    @Override
    public final void dispose() {
        if (disposed) return;
        disposed = true;

        disposeInner();
        nullify();
    }

    protected abstract void disposeInner();
}
