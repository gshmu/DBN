package com.dci.intellij.dbn.object.common.sorting;

import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.object.DBOrderedObject;
import com.dci.intellij.dbn.object.common.DBObject;
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

    private static final Latent<DBObjectComparator> classic = Latent.basic(() -> new Classic());
    private static final Latent<DBObjectComparator> grouped = Latent.basic(() -> new Grouped());

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

    public static <T extends DBObject> DBObjectComparator<T> basic(DBObjectType objectType) {
        Key key = Key.of(objectType, SortingType.NAME, true);
        return cast(REGISTRY.computeIfAbsent(key, k -> new Basic(k.getObjectType())));
    }

    public static <T extends DBObject> DBObjectComparator<T> classic() {
        return cast(classic.get());
    }

    public static <T extends DBObject> DBObjectComparator<T> grouped() {
        return cast(grouped.get());
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

    private static class Basic extends DBObjectComparator<DBObject> {
        protected Basic(DBObjectType objectType) {
            super(objectType, SortingType.NAME);
        }

        @Override
        public int compare(DBObject o1, DBObject o2) {
            return compareName(o1, o2);
        }
    }

    private static class Classic extends DBObjectComparator<DBObject> {
        protected Classic() {
            super(DBObjectType.ANY, SortingType.NAME);
        }

        @Override
        public int compare(DBObject o1, DBObject o2) {
            int result = compareType(o1, o2);
            if (result == 0) {
                result = compareName(o1, o2);
                if (result == 0) {
                    return compareOverload(o1, o2);
                }
                return result;
            }
            return result;
        }
    }

    private static class Grouped extends DBObjectComparator<DBObject> {
        protected Grouped() {
            super(DBObjectType.ANY, SortingType.NAME);
        }

        @Override
        public int compare(DBObject o1, DBObject o2) {
            return compareObject(o1, o2);
        }
    }

    public static int compareObject(@Nullable DBObject o1, @Nullable DBObject o2) {
        if (o1 == null && o2 == null) {
            return 0;
        } else  if (o1 == null) {
            return -1;
        } else if (o2 == null) {
            return 1;
        }
        return o1.ref().compareTo(o2.ref());
    }

    public static int compareType(DBObject o1, DBObject o2) {
        DBObjectType type1 = o1.getObjectType();
        DBObjectType type2 = o2.getObjectType();
        return type1.compareTo(type2);
    }

    public static int compareName(DBObject o1, DBObject o2) {
        String name1 = o1.getName();
        String name2 = o2.getName();
        return name1.compareToIgnoreCase(name2);
    }

    public static int compareOverload(DBObject o1, DBObject o2) {
        short overload1 = o1.getOverload();
        short overload2 = o2.getOverload();
        return Short.compare(overload1, overload2);
    }

    public static int comparePosition(DBOrderedObject o1, DBOrderedObject o2) {
        short position1 = o1.getPosition();
        short position2 = o2.getPosition();
        return Short.compare(position1, position2);
    }
}
