package com.dci.intellij.dbn.common.path;

public interface Node<T> {
    T getElement();

    Node<T> getParent();

    boolean isRecursive();

    boolean isAncestor(T element);

    void detach();
}
