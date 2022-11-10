package com.dci.intellij.dbn.common.lookup;

public interface Visitor<T> {
    void visit(T element);
}
