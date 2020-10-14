package com.dci.intellij.dbn.common.ui.tab;

import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.ui.DBNForm;
import com.dci.intellij.dbn.common.util.Unsafe;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBEditorTabs;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

public class TabbedPane extends JBEditorTabs implements StatefulDisposable {
    @Getter
    private boolean disposed;

    public TabbedPane(@NotNull DBNForm form) {
        super(form.getProject(), IdeFocusManager.findInstance(), form);
        Disposer.register(form, this);
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
        checkDisposed();
        return super.addTab(info, index);
    }

    @Override
    public TabInfo addTabSilently(TabInfo info, int index) {
        checkDisposed();
        return super.addTabSilently(info, index);
    }

    @NotNull
    @Override
    public TabInfo addTab(TabInfo info) {
        checkDisposed();
        return super.addTab(info);
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
    public void dispose() {
        if (!disposed) {
            disposed = true;
            Dispatch.runConditional(() -> {
                List<TabInfo> tabInfos = getTabs();
                for (TabInfo tabInfo : tabInfos) {
                    Unsafe.silent(() -> removeTab(tabInfo, true));
                }
            });
            nullify();
        }
    }
}
