package com.dci.intellij.dbn.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Chained<T extends Chained<T>> {
    private final T previous;
    private T next;
    private int index = -1;

    public Chained(T previous) {
        this.previous = previous;
        if (previous != null) {
            previous.setNext((T) this);
        }
    }

    public int getIndex() {
        if (index == -1) {
            index = previous == null ? 0 : previous.getIndex() + 1;
        }
        return index;
    }

    public boolean isLast() {
        return next == null;
    }

    public boolean isFirst() {
        return previous == null;
    }
}
