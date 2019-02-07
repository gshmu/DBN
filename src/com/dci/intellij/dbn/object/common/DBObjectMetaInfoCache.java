package com.dci.intellij.dbn.object.common;

import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.latent.MapLatent;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;

public abstract class DBObjectMetaInfoCache<T> extends DisposableBase {
    private MapLatent<DBObjectRef, T> cache = MapLatent.create(key -> init(key));

    private DBObjectMetaInfoCache() {}

    public T get(DBObjectRef objectRef) {
        return cache.get(objectRef);
    }
    protected abstract T init(DBObjectRef objectRef);

    public static <T> DBObjectMetaInfoCache<T> create(Initializer<T> initializer) {
        return new DBObjectMetaInfoCache<T>() {
            @Override
            protected T init(DBObjectRef objectRef) {
                return initializer.call(objectRef);
            }
        };
    }

    @FunctionalInterface
    public interface Initializer<R> {
        R call(DBObjectRef objectRef);
    }

    @Override
    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
            cache.clear();
        }

    }
}
