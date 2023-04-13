package com.dci.intellij.dbn.common.ui.tab;

import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.ui.form.DBNForm;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;

public class TabbedPane extends JBTabsImpl implements StatefulDisposable {
    @Getter
    private boolean disposed;

    public TabbedPane(@NotNull DBNForm form) {
        super(form.getProject(), IdeFocusManager.findInstance(), form);
        setTabDraggingEnabled(true);
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

    @Override
    public void dispose() {
        if (disposed) return;

        disposed = true;
        for (TabInfo tabInfo : myInfo2Label.keySet()) {
            Object object = tabInfo.getObject();
            tabInfo.setObject(null);
            Disposer.dispose(object);
        }
        nullify();
    }
}
