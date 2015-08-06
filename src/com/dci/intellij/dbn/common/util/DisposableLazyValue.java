package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;

public abstract class DisposableLazyValue<T> implements LazyValue<T>, Disposable{

    private T value;
    private boolean loaded = false;
    private boolean disposed = false;

    public DisposableLazyValue(Disposable parent) {
        Disposer.register(parent, this);
    }

    public final T get(){
        if (disposed) throw AlreadyDisposedException.INSTANCE;

        if (!loaded) {
            synchronized (this) {
                if (disposed) throw AlreadyDisposedException.INSTANCE;

                if (!loaded) {
                    value = load();
                    if (value instanceof Disposable) {
                        Disposer.register(this, (Disposable) value);
                    }
                    loaded = true;
                }
            }
        }
        return value;
    }

    public final void set(T value) {
        this.value = value;
        loaded = value != null;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public final void reset() {
        set(null);
    }

    protected abstract T load();

    @Override
    public void dispose() {
        disposed = true;
        value = null;
    }
}
