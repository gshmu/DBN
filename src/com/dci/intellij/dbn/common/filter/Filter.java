package com.dci.intellij.dbn.common.filter;

import java.util.List;

public interface Filter<T> {
    Filter NO_FILTER = new Filter() {
        @Override
        public boolean accepts(Object object) {
            return true;
        }

        @Override
        public boolean acceptsAll(List objects) {
            return true;
        }
    };

    public abstract boolean accepts(T object);
    default boolean acceptsAll(List<T> objects) {
        for (T object : objects) {
            if (!accepts(object)) return false;
        }
        return true;
    }
}
