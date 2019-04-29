package com.dci.intellij.dbn.common.cache;

import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class Cache {
    private Map<String, Object> elements = ContainerUtil.createSoftMap();

    @Nullable
    private <T> T get(String key) {
        return (T) elements.get(key);
    }

    private <T> void set(String key, T value) {
        elements.put(key, value);
    }

    public <T, E extends Throwable> T get(String key, ThrowableCallable<T, E> loader) throws E {
        T value = get(key);
        if (value == null) {
            synchronized (this) {
                value = get(key);
                if (value == null) {
                    value = loader.call();
                    set(key, value);
                }
            }
        }
        return value;
    }
}
