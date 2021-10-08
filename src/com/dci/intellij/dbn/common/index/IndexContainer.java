package com.dci.intellij.dbn.common.index;

import com.dci.intellij.dbn.common.util.Compactable;
import gnu.trove.TIntHashSet;

public class IndexContainer<T extends Indexable> implements Compactable {
    private final TIntHashSet INDEX = new TIntHashSet();

    public void put(T indexable) {
        int index = indexable.index();
        INDEX.add(index);
    }

    public boolean contains(T indexable) {
        return INDEX.contains(indexable.index());
    }

    @Override
    public void compact() {
        INDEX.trimToSize();
    }
}
