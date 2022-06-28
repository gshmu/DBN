package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.common.content.DynamicContentType;

import java.util.Arrays;

public class DynamicContentTypeIndex<O extends DynamicContentType, T extends DynamicContentType> {

    private final Class<O> ownerType;
    private final Entry<T>[] entries;

    public DynamicContentTypeIndex(Class<O> ownerType) {
        this.ownerType = ownerType;
        this.entries = new Entry[ownerType.getEnumConstants().length];
    }

    public int index(O ownerType, T type) {
        Entry<T> entry = entry(ownerType, (Class<T>) type.getClass());
        return entry.index(type);
    }

    private Entry<T> entry(O ownerType, Class<T> type) {
        int position = position(ownerType);
        if (entries[position] == null) {
            synchronized (this) {
                if (entries[position] == null) {
                    entries[position] = new Entry<>(type);
                }
            }
        }
        return entries[position];
    }

    private static class Entry<T extends DynamicContentType> {
        private final Class<T> type;
        private final int[] indexes;
        private int size = 0;

        public Entry(Class<T> type) {
            this.type = type;
            this.indexes = new int[type.getEnumConstants().length];
            Arrays.fill(indexes, -1);
        }

        public int index(T type) {
            int position = position(type);
            if (indexes[position] == -1) {
                synchronized (this) {
                    if (indexes[position] == -1) {
                        indexes[position] = size;
                        size++;
                    }
                }
            }

            return indexes[position];
        }

        @Override
        public String toString() {
            return type.getSimpleName() + " " + size;
        }
    }

    public String toString() {
        return ownerType.getSimpleName();
    }


    private static int position(DynamicContentType type) {
        Enum e = (Enum) type;
        return e.ordinal();
    }
}
