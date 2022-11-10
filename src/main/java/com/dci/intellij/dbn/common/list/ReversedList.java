package com.dci.intellij.dbn.common.list;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ReversedList<T> implements Iterable<T> {
    private final List<T> original;

    public ReversedList(List<T> original) {
        this.original = original;
    }

    @Override
    public Iterator<T> iterator() {
        final ListIterator<T> i = original.listIterator(original.size());
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return i.hasPrevious();
            }

            @Override
            public T next() {
                return i.previous();
            }

            @Override
            public void remove() {
                i.remove();
            }
        };
    }

    public static <T> ReversedList<T> get(List<T> original) {
        return new ReversedList<T>(original);
    }
}
