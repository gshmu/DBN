package com.dci.intellij.dbn.common.ui.tab;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.dispose.RegisteredDisposable;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBEditorTabs;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class TabbedPane extends JBEditorTabs implements RegisteredDisposable {
    private boolean disposed;

    public TabbedPane(@NotNull Disposable disposable) {
        super(null, ActionManager.getInstance(), null, disposable);
    }

    public void select(JComponent component, boolean requestFocus) {
        TabInfo tabInfo = findInfo(component);
        if (tabInfo != null) {
            select(tabInfo, requestFocus);
        }
    }

    @NotNull
    @Override
    public TabInfo addTab(TabInfo info, int index) {
        if (info.getComponent() != null) {
            registerDisposable(info);
            return super.addTab(info, index);
        }
        return info;
    }

    @NotNull
    @Override
    public TabInfo addTab(TabInfo info) {
        registerDisposable(info);
        return super.addTab(info);
    }

    @Override
    public TabInfo addTabSilently(TabInfo info, int index) {
        registerDisposable(info);
        return super.addTabSilently(info, index);
    }

    private void registerDisposable(TabInfo info) {
        Object object = info.getObject();
        if (object instanceof Disposable) {
            Disposable disposable = (Disposable) object;
            Disposer.register(this, disposable);
        }
    }

    @NotNull
    @Override
    public ActionCallback removeTab(TabInfo tabInfo) {
        return removeTab(tabInfo, true);
    }

    public ActionCallback removeTab(TabInfo tabInfo, boolean disposeComponent) {
        Object object = tabInfo.getObject();
        ActionCallback actionCallback = super.removeTab(tabInfo);
        if (disposeComponent && object instanceof Disposable) {
            Disposable disposable = (Disposable) object;
            Disposer.dispose(disposable);
            tabInfo.setObject(null);
        }
        return actionCallback;
    }

    @Override
    public void markDisposed() {
        disposed = true;
    }

/*
    @Override
    public boolean isDisposed() {
        return disposed;
    }
*/

    @Override
    public void dispose() {
        Dispatch.run(() -> TabbedPane.super.dispose());
        RegisteredDisposable.super.disposeInner();
    }
}
