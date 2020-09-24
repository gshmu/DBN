package com.dci.intellij.dbn.common.index;

import com.dci.intellij.dbn.common.util.Compactable;
import gnu.trove.TIntArrayList;

public class IndexedContainer<T extends Indexable> implements Compactable {
    private final TIntArrayList INDEX = new TIntArrayList();

    public void put(T indexable) {
        int index = indexable.getIdx();
        if (!INDEX.contains(index)) {
            INDEX.add(index);
        }
    }

    public boolean contains(T indexable) {
        return INDEX.contains(indexable.getIdx());
    }

    @Override
    public void compact() {
        INDEX.trimToSize();
    }
}
