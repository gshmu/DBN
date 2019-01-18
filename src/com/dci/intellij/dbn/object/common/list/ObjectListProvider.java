package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.object.common.DBObject;

import java.util.List;

@FunctionalInterface
public interface ObjectListProvider<T extends DBObject> {
    List<T> getObjects();
}
