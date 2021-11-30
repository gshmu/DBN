package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;

import java.util.List;

public class DBObjectNavigationListImpl<T extends DBObject> implements DBObjectNavigationList<T> {
    private final String name;
    private final DBObjectRef<T> object;
    private final List<DBObjectRef<T>> objects;
    private ObjectListProvider<T> objectsProvider;

    public DBObjectNavigationListImpl(String name, T object) {
        this.name = name;
        this.object = DBObjectRef.of(object);
        this.objects = null;
    }

    public DBObjectNavigationListImpl(String name, List<T> objects) {
        this.name = name;
        this.object = null;
        this.objects = DBObjectRef.from(objects);
    }

    public DBObjectNavigationListImpl(String name, ObjectListProvider<T> objectsProvider) {
        this.name = name;
        this.object = null;
        this.objects = null;
        this.objectsProvider = objectsProvider;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public T getObject() {
        return DBObjectRef.get(object);
    }

    @Override
    public List<T> getObjects() {
        return objects == null ? null : DBObjectRef.get(objects);
    }

    @Override
    public ObjectListProvider<T> getObjectsProvider() {
        return objectsProvider;
    }

    @Override
    public boolean isLazy() {
        return objectsProvider != null;
    }
}
