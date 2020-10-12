package com.dci.intellij.dbn.common.dispose;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public interface DisposableContainer {
    static <T extends Disposable> List<T> list(Disposable parent) {
        return new Impl.DisposableList<>(parent);
    }

    static <T extends Disposable> List<T> concurrentList(Disposable parent) {
        return new Impl.DisposableConcurrentList<>(parent);
    }

    static <K, V extends Disposable> Map<K, V> map(Disposable parent) {
        return new Impl.DisposableMap<>(parent);
    }

    abstract class Impl {
        private static class DisposableList<T extends Disposable> extends ArrayList<T> implements Disposable{
            public DisposableList(@NotNull Disposable parent) {
                Disposer.register(parent, this);
            }

            @Override
            public void dispose() {
                for (T disposable : this) {
                    Disposer.dispose(disposable);
                }
                clear();
            }

            @Override
            public T remove(int index) {
                T removed = super.remove(index);
                Disposer.dispose(removed);
                return removed;
            }

            @Override
            public boolean remove(Object o) {
                boolean removed = super.remove(o);
                if (removed && o instanceof Disposable) {
                    Disposable disposable = (Disposable) o;
                    Disposer.dispose(disposable);
                }
                return removed;
            }
        }

        private static class DisposableConcurrentList<T extends Disposable> extends CopyOnWriteArrayList<T> implements Disposable{
            public DisposableConcurrentList(@NotNull Disposable parent) {
                Disposer.register(parent, this);
            }

            @Override
            public void dispose() {
                for (T disposable : this) {
                    Disposer.dispose(disposable);
                }
                clear();
            }

            @Override
            public T remove(int index) {
                T removed = super.remove(index);
                Disposer.dispose(removed);
                return removed;
            }

            @Override
            public boolean remove(Object o) {
                boolean removed = super.remove(o);
                if (removed && o instanceof Disposable) {
                    Disposable disposable = (Disposable) o;
                    Disposer.dispose(disposable);
                }
                return removed;
            }
        }


        private static class DisposableMap<K, V extends Disposable> extends HashMap<K, V> implements Disposable{
            public DisposableMap(@NotNull Disposable parent) {
                Disposer.register(parent, this);
            }

            @Override
            public void dispose() {
                for (V disposable : values()) {
                    Disposer.dispose(disposable);
                }
                clear();
            }

            @Override
            public boolean remove(Object key, Object value) {
                boolean removed = super.remove(key, value);
                if (removed && value instanceof Disposable) {
                    Disposable disposable = (Disposable) value;
                    Disposer.dispose(disposable);
                }
                return removed;
            }
        }
    }
}
