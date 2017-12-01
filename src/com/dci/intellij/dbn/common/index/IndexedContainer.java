package com.dci.intellij.dbn.common.index;

import gnu.trove.TIntArrayList;

public class IndexedContainer<T extends Indexable> {
    private TIntArrayList INDEX = new TIntArrayList();

    public void put(T indexable) {
        int index = indexable.getIdx();
        if (!INDEX.contains(index)) {
            INDEX.add(index);
        }
    }

    public boolean contains(T indexable) {
        return INDEX.contains(indexable.getIdx());
    }
}
