package com.dci.intellij.dbn.browser.options;

import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.Nullable;

import java.util.EventListener;

public interface ObjectFilterChangeListener extends EventListener {
    Topic<ObjectFilterChangeListener> TOPIC = Topic.create("Object filter changed", ObjectFilterChangeListener.class);
    void typeFiltersChanged();
    void nameFiltersChanged(@Nullable DBObjectType objectType);
}
