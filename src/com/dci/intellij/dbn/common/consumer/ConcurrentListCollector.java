package com.dci.intellij.dbn.common.consumer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @deprecated slow and memory demanding for high concurrency
 */
public class ConcurrentListCollector<T> extends ListCollector<T> {

    protected ConcurrentListCollector() {}

    public static <T> ListCollector<T> basic() {
        return new ConcurrentListCollector<>();
    }

    public static <T> ListCollector<T> unique() {
        return new ConcurrentListCollector<T>() {
            @Override
            public void consume(T element) {
                if (!elements().contains(element)) {
                    super.consume(element);
                }
            }
        };
    }

    @Override
    protected List<T> createList() {
        return new CopyOnWriteArrayList<>();
    }
}
