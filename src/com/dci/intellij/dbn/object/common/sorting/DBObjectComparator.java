package com.dci.intellij.dbn.object.common.sorting;

import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBVirtualObject;
import com.dci.intellij.dbn.object.type.DBObjectType;
import lombok.Getter;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.dci.intellij.dbn.common.util.Unsafe.cast;

@Getter
public abstract class DBObjectComparator<T extends DBObject> implements Comparator<T> {
    private static final Map<SortingKey, DBObjectComparator<?>> REGISTRY = new ConcurrentHashMap<>();

    @Delegate
    private final SortingKey key;

    protected DBObjectComparator(DBObjectType objectType, SortingType sortingType) {
        this(objectType, sortingType, false);

    }

    private DBObjectComparator(DBObjectType objectType, SortingType sortingType, boolean virtual) {
        this(SortingKey.of(objectType, sortingType, virtual));

        if (!virtual) {
            REGISTRY.putIfAbsent(SortingKey.of(objectType, sortingType), this);
        }
    }

    private DBObjectComparator(SortingKey key) {
        this.key = key;
    }

    @Nullable
    public static <T extends DBObject> DBObjectComparator<T> get(DBObjectType objectType, SortingType sortingType) {
        SortingKey key = SortingKey.of(objectType, sortingType);
        return cast(REGISTRY.get(key));
    }

    public static <T extends DBObject> DBObjectComparator<T> virtual(DBObjectType objectType, SortingType sortingType) {
        SortingKey key = SortingKey.of(objectType, sortingType, true);
        return cast(REGISTRY.computeIfAbsent(key, k -> new DBObjectComparator<DBVirtualObject>(k) {
            @Override
            public int compare(DBVirtualObject o1, DBVirtualObject o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        }));
    }
}
