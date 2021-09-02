package com.dci.intellij.dbn.common.filter;

import com.dci.intellij.dbn.common.sign.Signed;

import java.util.Collection;

public interface Filter<T> extends Signed {
    Filter NO_FILTER = new Filter() {
        @Override
        public int getSignature() {
            return 0;
        }

        @Override
        public boolean accepts(Object object) {
            return true;
        }

        @Override
        public boolean acceptsAll(Collection objects) {
            return true;
        }
    };

    boolean accepts(T object);

    @Override
    default int getSignature() {
        return hashCode();
    }

    default boolean acceptsAll(Collection<T> objects) {
        for (T object : objects) {
            if (!accepts(object)) return false;
        }
        return true;
    }
}
