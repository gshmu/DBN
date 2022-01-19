package com.dci.intellij.dbn.common.content;

public interface DynamicContentType<T extends DynamicContentType<T>> {
    default boolean matches(T contentType) {
        return false;
    }

    default T getGenericType() {
        return null;
    };

    DynamicContentType NULL = new DynamicContentType() {
        @Override
        public boolean matches(DynamicContentType contentType) {
            return contentType == this;
        }

        @Override
        public String toString() {
            return "NULL";
        }
    };
}
