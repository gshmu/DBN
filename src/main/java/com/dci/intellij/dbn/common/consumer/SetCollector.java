package com.dci.intellij.dbn.common.consumer;

import com.dci.intellij.dbn.common.util.Commons;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class SetCollector<T> implements Consumer<T> {
    private Set<T> elements;

    protected SetCollector() {}


    public static <T> SetCollector<T> create() {
        return new SetCollector<>();
    }

    @Override
    public void accept(T element) {
        if (elements == null) {
            elements = createSet();
        }
        elements.add(element);
    }

    protected Set<T> createSet() {
        return new HashSet<>();
    }

    public Set<T> elements() {
        return Commons.nvl(elements, Collections.emptySet());
    }

    public boolean isEmpty() {
        return elements == null || elements.isEmpty();
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }
}
