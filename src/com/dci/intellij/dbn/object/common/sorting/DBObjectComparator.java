package com.dci.intellij.dbn.object.common.sorting;

import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.type.DBObjectType;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Getter
public abstract class DBObjectComparator<T extends DBObject> implements Comparator<T> {
    private static final List<DBObjectComparator> REGISTRY = Arrays.asList(
            new DBColumnNameComparator(),
            new DBColumnPositionComparator(),
            new DBProcedureNameComparator(),
            new DBProcedurePositionComparator(),
            new DBFunctionNameComparator(),
            new DBFunctionPositionComparator(),
            new DBArgumentNameComparator(),
            new DBArgumentPositionComparator());

    private final DBObjectType objectType;
    private final SortingType sortingType;

    public DBObjectComparator(DBObjectType objectType, SortingType sortingType) {
        this.objectType = objectType;
        this.sortingType = sortingType;
    }


    @Nullable
    public static DBObjectComparator get(DBObjectType objectType, SortingType sortingType) {
        for (DBObjectComparator comparator : REGISTRY) {
            if (comparator.objectType == objectType && comparator.sortingType == sortingType) {
                return comparator;
            }
        }
        return null;
    }

    public static List<SortingType> getSortingTypes(DBObjectType objectType) {
        List<SortingType> sortingTypes = new ArrayList<>();
        for (DBObjectComparator comparator : REGISTRY) {
            if (comparator.objectType == objectType) {
                sortingTypes.add(comparator.sortingType);
            }
        }
        return sortingTypes;
    }

}
