package com.dci.intellij.dbn.common.property;

/**
 * use for avoiding stack overflows if can result in recursive call chains
 */
public abstract class BasicProperty<T> {
    private T value;
    private boolean loading;

    public BasicProperty(T initial) {
        this.value = initial;
    }

    private boolean set(T value) {
        if (!this.value.equals(value)) {
            this.value = value;
            return true;
        }
        return false;
    }

    public synchronized T get() {
        if (!loading) {
            try {
                loading = true;
                value = load();
            } finally {
                loading = false;
            }
        }

        return value;
    }

    protected abstract T load();
}
