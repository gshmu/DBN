package com.dci.intellij.dbn.common.ui.util;

import com.dci.intellij.dbn.common.collections.WeakHashSet;
import com.dci.intellij.dbn.common.routine.Consumer;

import java.util.EventListener;
import java.util.Set;

public class Listeners {
    public static <T extends EventListener> Set<T> container() {
        return new WeakHashSet<>();
    }

    public static <T extends EventListener> void notify(Set<T> listeners, Consumer<T> notifier) {
        if (listeners.isEmpty()) return;
        listeners.stream().filter(l -> l != null).forEach(notifier);
    }

}
