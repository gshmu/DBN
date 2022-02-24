package com.dci.intellij.dbn.common;

public interface Referenceable<R extends Reference> {
    R ref();
    String getName();
}
