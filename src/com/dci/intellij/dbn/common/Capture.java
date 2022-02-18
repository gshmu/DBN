package com.dci.intellij.dbn.common;

import java.util.Objects;
import java.util.function.Supplier;

public class Capture<T> {
    private T value;
    private Object check;
    protected volatile boolean loading;

    public boolean isValid(Object check) {
        return Objects.equals(this.check, check);
    }

    public T get() {
        return value;
    }

    public void capture(Object check, Supplier<T> value) {
        this.check = check;
        if (!loading) {
            synchronized (this) {
                if (!loading) {
                    try {
                        loading = true;
                        this.value = value.get();
                    } finally {
                        loading = false;
                    }
                }
            }
        }
    }


}
