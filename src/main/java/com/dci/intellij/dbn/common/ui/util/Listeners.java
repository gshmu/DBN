package com.dci.intellij.dbn.common.ui.util;

import com.dci.intellij.dbn.common.dispose.Disposed;
import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.routine.Consumer;
import com.intellij.openapi.Disposable;
import com.intellij.util.containers.ContainerUtil;

import java.util.EventListener;
import java.util.Set;

import static com.dci.intellij.dbn.common.dispose.Failsafe.guarded;

public class Listeners<T extends EventListener> {
    private Set<T> container = ContainerUtil.newConcurrentSet();

    public static <T extends EventListener> Listeners<T> create() {
        return new Listeners<>();
    }

    public static <T extends EventListener> Listeners<T> create(Disposable parentDisposable) {
        Listeners<T> listeners = new Listeners<>();
        Disposer.register(parentDisposable, () -> listeners.container = Disposed.set());
        return listeners;
    }

    public void add(T listener) {
        container.add(listener);
    }

    public void remove(T listener) {
        container.remove(listener);
    }

    public void notify(Consumer<T> notifier) {
        container.stream().filter(l -> l != null).forEach(l -> guarded(() -> notifier.accept(l)));
    }

    public void clear() {
        container.clear();
    }

    public boolean isEmpty() {
        return container.isEmpty();
    }
}
