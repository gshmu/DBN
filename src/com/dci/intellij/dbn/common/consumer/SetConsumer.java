package com.dci.intellij.dbn.common.consumer;

import com.dci.intellij.dbn.common.util.CommonUtil;
import com.intellij.util.Consumer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SetConsumer<T> implements Consumer<T> {
    private Set<T> elements;

    private SetConsumer() {}


    public static <T> SetConsumer<T> create() {
        return new SetConsumer<>();
    }

    @Override
    public void consume(T element) {
        if (elements == null) {
            elements = new HashSet<>();
        }
        elements.add(element);
    }

    public Set<T> elements() {
        return CommonUtil.nvl(elements, Collections.emptySet());
    }

    public boolean isEmpty() {
        return elements == null || elements.isEmpty();
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }
}
