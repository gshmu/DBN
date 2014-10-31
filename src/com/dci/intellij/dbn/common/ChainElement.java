package com.dci.intellij.dbn.common;

public class ChainElement<T extends ChainElement<T>> {
    private T previous;
    private T next;

    public ChainElement(T previous) {
        this.previous = previous;
        if (previous != null) {
            previous.setNext((T) this);
        }
    }

    public T getPrevious() {
        return previous;
    }

    public T getNext() {
        return next;
    }

    void setNext(T next) {
        this.next = next;
    }

    public boolean isLast() {
        return next == null;
    }

    public boolean isFirst() {
        return previous == null;
    }
}
