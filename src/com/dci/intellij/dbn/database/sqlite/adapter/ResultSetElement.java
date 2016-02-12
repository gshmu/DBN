package com.dci.intellij.dbn.database.sqlite.adapter;

public interface ResultSetElement<T extends ResultSetElement> extends Comparable<T> {
    String getName();
}
