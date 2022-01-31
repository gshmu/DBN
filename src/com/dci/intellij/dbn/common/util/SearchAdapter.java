package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.object.DBType;
import com.dci.intellij.dbn.object.common.DBObject;

public interface SearchAdapter<T> {
    int compare(T element);


    static <O extends DBObject> SearchAdapter<O> forObject(String name, short overload) {
        return object -> {
            String objName = object.getName();
            short objectOverload = object.getOverload();


            int result = objName.compareToIgnoreCase(name);
            return result == 0 ? objectOverload - overload : result;
        };
    }

    static <O extends DBObject> SearchAdapter<O> forType(String name, short overload, boolean collection) {
        return object -> {
            if (((DBType) object).isCollection() == collection) {
                int result = object.getName().compareToIgnoreCase(name);
                return result == 0 ? object.getOverload() - overload : result;
            } else {
                return collection ? -1 : 1;
            }
        };
    }
}
