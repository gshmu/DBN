package com.dci.intellij.dbn.browser.options;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.filter.Filter;
import com.intellij.util.messages.Topic;

import java.util.EventListener;

public interface ObjectFilterChangeListener extends EventListener {
    Topic<ObjectFilterChangeListener> TOPIC = Topic.create("Object filter changed", ObjectFilterChangeListener.class);
    void filterChanged(Filter<BrowserTreeNode> filter);
}
