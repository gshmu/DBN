package com.dci.intellij.dbn.common.count;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Counters {
    private final Map<CounterType, Counter> counters = new ConcurrentHashMap<>();

    public Counter get(CounterType type) {
        return counters.computeIfAbsent(type, t -> new Counter(t));
    }
}
