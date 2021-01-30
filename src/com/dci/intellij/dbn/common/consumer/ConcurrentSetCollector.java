package com.dci.intellij.dbn.common.consumer;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentSetCollector<T> extends SetCollector<T> {

    protected ConcurrentSetCollector() {}

    public static <T> SetCollector<T> create() {
        return new ConcurrentSetCollector<>();
    }

    @Override
    protected Set<T> createSet() {
        return Collections.newSetFromMap(new ConcurrentHashMap<>());
    }
}
