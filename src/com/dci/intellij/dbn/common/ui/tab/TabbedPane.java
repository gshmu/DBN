package com.dci.intellij.dbn.common.ui.tab;

import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBTabsImpl;

public class TabbedPane extends JBTabsImpl {
    public TabbedPane(@NotNull Disposable disposable) {
        super(null, null, disposable);
    }

    public void select(JComponent component, boolean requestFocus) {
        TabInfo tabInfo = findInfo(component);
        if (tabInfo != null) {
            select(tabInfo, requestFocus);
        }
    }

    @NotNull
    @Override
    public ActionCallback removeTab(TabInfo tabInfo) {
        Object object = tabInfo.getObject();
        ActionCallback actionCallback = super.removeTab(tabInfo);
        if (object instanceof Disposable) {
            Disposable disposable = (Disposable) object;
            Disposer.dispose(disposable);
            tabInfo.setObject(null);
        }
        return actionCallback;
    }

    @Override
    public void dispose() {
        for (TabInfo tabInfo : getTabs()) {
            Object object = tabInfo.getObject();
            if (object instanceof Disposable) {
                Disposable disposable = (Disposable) object;
                Disposer.dispose(disposable);
                tabInfo.setObject(null);
            }
        }
        super.dispose();
    }
}
