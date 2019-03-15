package com.dci.intellij.dbn.common;

public class Chained<T extends Chained<T>> {
    private T previous;
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
            synchronized (this) {
                if (index == -1) {
                    T child = (T) this;
                    while (child != null) {
                        index++;
                        child = child.getPrevious();
                    }
                }
            }
        }
        return index;
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
