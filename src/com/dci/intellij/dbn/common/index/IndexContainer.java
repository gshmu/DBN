package com.dci.intellij.dbn.common.index;

import com.dci.intellij.dbn.common.util.Compactable;
import gnu.trove.THashSet;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

public class IndexContainer<T extends Indexable> implements Compactable {
    private final TIntHashSet INDEX = new TIntHashSet();

    public void add(T indexable) {
        int index = indexable.index();
        INDEX.add(index);
    }

    public boolean isEmpty() {
        return INDEX.isEmpty();
    }

    public boolean contains(T indexable) {
        return INDEX.contains(indexable.index());
    }

    public Set<T> elements(Function<Integer, T> resolver) {
        if (INDEX.isEmpty()) {
            return Collections.emptySet();
        } else {
            Set<T> elements = new THashSet<>(INDEX.size());
            TIntIterator iterator = INDEX.iterator();
            while (iterator.hasNext()) {
                int next = iterator.next();
                elements.add(resolver.apply(next));
            }
            return elements;
        }
    }

    @Override
    public void compact() {
        INDEX.trimToSize();
    }

    public void addAll(Set<T> elements) {
        elements.forEach(element -> add(element));
    }
}
