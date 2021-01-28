package com.dci.intellij.dbn.common.consumer;

import com.dci.intellij.dbn.common.util.CommonUtil;
import com.intellij.util.Consumer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListConsumer<T> implements Consumer<T> {
    private List<T> elements;

    private ListConsumer() {}


    public static <T> ListConsumer<T> basic() {
        return new ListConsumer<>();
    }

    public static <T> ListConsumer<T> unique() {
        return new ListConsumer<T>() {
            @Override
            public void consume(T element) {
                if (!elements().contains(element)) {
                    super.consume(element);
                }
            }
        };
    }

    @Override
    public void consume(T element) {
        if (elements == null) {
            elements = new ArrayList<>();
        }
        elements.add(element);
    }

    public List<T> elements() {
        return CommonUtil.nvl(elements, Collections.emptyList());
    }

    public boolean isEmpty() {
        return elements == null || elements.isEmpty();
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }
}
