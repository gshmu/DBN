package com.dci.intellij.dbn.common.index;

import com.dci.intellij.dbn.common.thread.ReadWriteMonitor;
import com.dci.intellij.dbn.common.util.Compactable;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class IndexContainer<T extends Indexable> implements Compactable {
    private final TIntHashSet INDEX = new TIntHashSet();
    private final ReadWriteMonitor monitor = new ReadWriteMonitor();

    public void add(T indexable) {
        monitor.write(() -> INDEX.add(indexable.index()));
    }

    public boolean isEmpty() {
        return monitor.read(() -> INDEX.isEmpty());
    }

    public boolean contains(T indexable) {
        return monitor.read(() -> INDEX.contains(indexable.index()));
    }

    public Set<T> elements(Function<Integer, T> resolver) {
        return monitor.read(() -> {
            if (INDEX.isEmpty()) {
                return Collections.emptySet();
            } else {
                Set<T> elements = new HashSet<>(INDEX.size());
                TIntIterator iterator = INDEX.iterator();
                while (iterator.hasNext()) {
                    int next = iterator.next();
                    elements.add(resolver.apply(next));
                }
                return elements;
            }
        });
    }

    @Override
    public void compact() {
        monitor.write(() -> INDEX.trimToSize());;
    }

    public void addAll(Set<T> elements) {
        monitor.write(() -> elements.forEach(element -> add(element)));
    }
}
