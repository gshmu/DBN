package com.dci.intellij.dbn.browser.model;

import com.dci.intellij.dbn.common.ui.tree.TreeEventType;

public abstract class BrowserTreeEventAdapter implements BrowserTreeEventListener{

    @Override
    public void nodeChanged(BrowserTreeNode node, TreeEventType eventType) {

    }

    @Override
    public void selectionChanged() {

    }
}
