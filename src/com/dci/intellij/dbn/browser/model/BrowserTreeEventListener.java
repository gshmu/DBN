package com.dci.intellij.dbn.browser.model;

import java.util.EventListener;

import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.intellij.util.messages.Topic;

public interface BrowserTreeEventListener extends EventListener{
    Topic<BrowserTreeEventListener> TOPIC = Topic.create("Browser tree event", BrowserTreeEventListener.class);

    void nodeChanged(BrowserTreeNode node, TreeEventType eventType);

    void selectionChanged();
}
