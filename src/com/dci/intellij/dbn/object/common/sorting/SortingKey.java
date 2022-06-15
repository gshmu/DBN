package com.dci.intellij.dbn.object.common.sorting;

import com.dci.intellij.dbn.object.type.DBObjectType;
import lombok.Value;

@Value
public class SortingKey {
    private final DBObjectType objectType;
    private final SortingType sortingType;
    private final boolean virtual;

    public static SortingKey of(DBObjectType objectType, SortingType sortingType) {
        return of(objectType, sortingType, false);
    }

    public static SortingKey of(DBObjectType objectType, SortingType sortingType, boolean virtual) {
        return new SortingKey(objectType, sortingType, virtual);
    }
}
