package com.dci.intellij.dbn.object.common.sorting;

import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBVirtualObject;
import com.dci.intellij.dbn.object.type.DBObjectType;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.dci.intellij.dbn.common.util.Unsafe.cast;

@Getter
public abstract class DBObjectComparator<T extends DBObject> implements Comparator<T> {
    private static final Map<Key, DBObjectComparator<?>> REGISTRY = new ConcurrentHashMap<>();
    static {
        new DBColumnNameComparator();
        new DBColumnPositionComparator();
        new DBProcedureNameComparator();
        new DBProcedurePositionComparator();
        new DBFunctionNameComparator();
        new DBFunctionPositionComparator();
        new DBArgumentNameComparator();
        new DBArgumentPositionComparator();
    }

    @Delegate
    private final Key key;

    protected DBObjectComparator(DBObjectType objectType, SortingType sortingType) {
        this(objectType, sortingType, false);

    }

    private DBObjectComparator(DBObjectType objectType, SortingType sortingType, boolean virtual) {
        this(Key.of(objectType, sortingType, virtual));

        if (!virtual) {
            REGISTRY.putIfAbsent(Key.of(objectType, sortingType), this);
        }
    }

    private DBObjectComparator(Key key) {
        this.key = key;
    }

    @Nullable
    public static <T extends DBObject> DBObjectComparator<T> get(DBObjectType objectType, SortingType sortingType) {
        Key key = Key.of(objectType, sortingType);
        return cast(REGISTRY.get(key));
    }

    public static <T extends DBObject> DBObjectComparator<T> virtual(DBObjectType objectType, SortingType sortingType) {
        Key key = Key.of(objectType, sortingType, true);
        return cast(REGISTRY.computeIfAbsent(key, k -> new DBObjectComparator<DBVirtualObject>(k) {
            @Override
            public int compare(DBVirtualObject o1, DBVirtualObject o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        }));
    }

    @Value
    private static class Key {
        private final DBObjectType objectType;
        private final SortingType sortingType;
        private final boolean virtual;

        public static Key of(DBObjectType objectType, SortingType sortingType) {
            return of(objectType, sortingType, false);
        }

        public static Key of(DBObjectType objectType, SortingType sortingType, boolean virtual) {
            return new Key(objectType, sortingType, virtual);
        }
    }
}
