package com.dci.intellij.dbn.common.ui.tab;

import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.ui.form.DBNForm;
import com.dci.intellij.dbn.common.util.Commons;
import com.dci.intellij.dbn.common.util.Unsafe;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import static com.dci.intellij.dbn.common.util.Commons.nvl;

public class TabbedPane extends JBTabsImpl implements StatefulDisposable {
    private boolean disposed;

    public TabbedPane(@NotNull DBNForm form) {
        super(form.ensureProject());
        setTabDraggingEnabled(true);
        Disposer.register(form, () -> customDispose());
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
        if (disposeComponent) {
            Disposer.dispose(object);
            tabInfo.setObject(null);
        }
        return actionCallback;
    }

    public void selectTab(String tabName) {
        if (tabName == null) return;

        for (TabInfo tabInfo : getTabs()) {
            if (Objects.equals(tabInfo.getText(), tabName)) {
                select(tabInfo, false);
                return;
            }
        }
    }

    public String getSelectedTabName() {
        TabInfo selectedInfo = getSelectedInfo();
        if (selectedInfo == null) return null;

        return selectedInfo.getText();
    }

    private void customDispose() {
        if (disposed) return;

        disposed = true;
        Collection<TabInfo> tabInfos = nvl(Commons.coalesce(
                () -> Unsafe.silent(null, () -> myInfo2Label.keySet()),
                () -> Unsafe.silent(null, () -> getTabs())), Collections.emptyList());
        for (TabInfo tabInfo : tabInfos) {
            Object object = tabInfo.getObject();
            tabInfo.setObject(null);
            Disposer.dispose(object);
        }
        nullify();
    }
}
