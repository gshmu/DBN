package com.dci.intellij.dbn.common.content;

public interface DynamicContentType<T extends DynamicContentType<T>> {
    default boolean matches(T contentType) {
        return false;
    }

    default T getGenericType() {
        return null;
    };
}
