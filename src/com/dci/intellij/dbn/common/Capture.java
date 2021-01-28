package com.dci.intellij.dbn.common;

import java.util.Objects;

public class Capture<T> {
    private T value;
    private Object check;

    public boolean valid(Object check) {
        return Objects.equals(this.check, check);
    }

    public T get() {
        return value;
    }

    public void capture(T value, Object check) {
        this.value = value;
        this.check = check;
    }


}
