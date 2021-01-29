package com.dci.intellij.dbn.common.consumer;

import com.dci.intellij.dbn.common.util.CommonUtil;
import com.intellij.util.Consumer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListCollector<T> implements Consumer<T> {
    private List<T> elements;

    protected ListCollector() {}

    public static <T> ListCollector<T> basic() {
        return new ListCollector<>();
    }

    public static <T> ListCollector<T> unique() {
        return new ListCollector<T>() {
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
            elements = createList();
        }
        elements.add(element);
    }

    protected List<T> createList() {
        return new ArrayList<>();
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
