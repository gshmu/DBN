package com.dci.intellij.dbn.common.constant;


public interface Constant<T> {
    String id();
    boolean isOneOf(T... constants);
}
