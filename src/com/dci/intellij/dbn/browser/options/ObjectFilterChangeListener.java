package com.dci.intellij.dbn.browser.options;

import java.util.EventListener;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.util.messages.Topic;

public interface ObjectFilterChangeListener extends EventListener {
    Topic<ObjectFilterChangeListener> TOPIC = Topic.create("Object filter changed", ObjectFilterChangeListener.class);
    void typeFiltersChanged(String connectionId);
    void nameFiltersChanged(@Nullable DBObjectType objectType, String connectionId);
}
