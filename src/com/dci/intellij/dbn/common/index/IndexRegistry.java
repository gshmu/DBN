package com.dci.intellij.dbn.common.index;

import com.dci.intellij.dbn.common.thread.ReadWriteMonitor;
import gnu.trove.TIntObjectHashMap;

public class IndexRegistry<T extends Indexable> {
    private final ReadWriteMonitor monitor = new ReadWriteMonitor();
    private final TIntObjectHashMap<T> INDEX = new TIntObjectHashMap<>();

    public void add(T element) {
        monitor.write(() -> INDEX.put(element.index(), element));
    }

    public T get(int index) {
        return monitor.read(() -> INDEX.get(index));
    }
}
