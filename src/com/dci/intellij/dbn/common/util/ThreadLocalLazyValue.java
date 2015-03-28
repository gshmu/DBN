package com.dci.intellij.dbn.common.util;

public abstract class ThreadLocalLazyValue<T> extends ThreadLocal<T>{

    public final T get(){
        T value = super.get();
        if (value == null) {
            synchronized (this) {
                value = super.get();
                if (value == null) {
                    value = create();
                    set(value);
                }
            }
        }
        return value;
    }

    protected abstract T create();
}
