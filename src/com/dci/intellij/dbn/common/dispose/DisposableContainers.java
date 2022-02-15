package com.dci.intellij.dbn.common.dispose;

import com.intellij.openapi.Disposable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public final class DisposableContainers {
    private DisposableContainers() {}

    public static <T extends Disposable> List<T> list(Disposable parent) {
        return new DisposableList<>(parent);
    }

    public static <T extends Disposable> List<T> concurrentList(Disposable parent) {
        return new DisposableConcurrentList<>(parent);
    }

    public static <K, V extends Disposable> Map<K, V> map(Disposable parent) {
        return new DisposableMap<>(parent);
    }

    private static class DisposableList<T extends Disposable> extends ArrayList<T> implements Disposable{
        public DisposableList(@NotNull Disposable parent) {
            SafeDisposer.register(parent, this);
        }

        @Override
        public void dispose() {
            SafeDisposer.disposeCollection(this);
        }

        @Override
        public T remove(int index) {
            T removed = super.remove(index);
            SafeDisposer.dispose(removed);
            return removed;
        }

        @Override
        public boolean remove(Object o) {
            boolean removed = super.remove(o);
            if (removed && o instanceof Disposable) {
                Disposable disposable = (Disposable) o;
                SafeDisposer.dispose(disposable);
            }
            return removed;
        }
    }

    private static class DisposableConcurrentList<T extends Disposable> extends CopyOnWriteArrayList<T> implements Disposable{
        public DisposableConcurrentList(@NotNull Disposable parent) {
            SafeDisposer.register(parent, this);
        }

        @Override
        public void dispose() {
            SafeDisposer.disposeCollection(this);
        }

        @Override
        public T remove(int index) {
            T removed = super.remove(index);
            SafeDisposer.dispose(removed);
            return removed;
        }

        @Override
        public boolean remove(Object o) {
            boolean removed = super.remove(o);
            if (removed && o instanceof Disposable) {
                Disposable disposable = (Disposable) o;
                SafeDisposer.dispose(disposable);
            }
            return removed;
        }
    }


    private static class DisposableMap<K, V extends Disposable> extends HashMap<K, V> implements Disposable{
        public DisposableMap(@NotNull Disposable parent) {
            SafeDisposer.register(parent, this);
        }

        @Override
        public void dispose() {
            SafeDisposer.disposeMap(this);
        }

        @Override
        public boolean remove(Object key, Object value) {
            boolean removed = super.remove(key, value);
            if (removed && value instanceof Disposable) {
                Disposable disposable = (Disposable) value;
                SafeDisposer.dispose(disposable);
            }
            return removed;
        }
    }
}
