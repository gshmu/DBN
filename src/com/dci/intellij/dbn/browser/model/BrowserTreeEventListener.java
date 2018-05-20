package com.dci.intellij.dbn.browser.model;

import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.intellij.util.messages.Topic;

import java.util.EventListener;

public interface BrowserTreeEventListener extends EventListener{
    Topic<BrowserTreeEventListener> TOPIC = Topic.create("Browser tree event", BrowserTreeEventListener.class);

    void nodeChanged(BrowserTreeNode node, TreeEventType eventType);

    void selectionChanged();
}
