package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.list.FiltrableList;
import com.dci.intellij.dbn.common.routine.ParametricRunnable;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CollectionUtil {
    public static <T extends Cloneable<T>> void cloneElements(Collection<T> source, Collection<T> target) {
        for (T cloneable : source) {
            T clone = cloneable.clone();
            target.add(clone);
        }
    }

    public static void clear(Collection collection) {
        if (collection != null && collection.size() > 0) {
            collection.clear();
        }
    }

    public static void clear(@Nullable Map map) {
        if (map != null) {
            map.clear();
        }
    }

    public static void compactRecursive(@Nullable List<? extends Compactable> elements) {
        if (elements != null) {
            compact(elements);
            for (Compactable element : elements) {
                element.compact();
            }
        }
    }

    public static void compact(Compactable compactable) {
        if (compactable != null) compactable.compact();
    }

    public static void compact(@Nullable Collection elements) {
        if (elements != null) {
            if (elements instanceof ArrayList) {
                ArrayList arrayList = (ArrayList) elements;
                arrayList.trimToSize();
            } else if (elements instanceof FiltrableList) {
                FiltrableList filtrableList = (FiltrableList) elements;
                filtrableList.trimToSize();
            } else if (elements instanceof THashSet) {
                THashSet hashSet = (THashSet) elements;
                hashSet.trimToSize();
            }  else if (elements instanceof THashMap) {
                THashMap hashMap = (THashMap) elements;
                hashMap.trimToSize();
            }

        }
    }

    public static void compact(@Nullable Map elements) {
        if (elements != null) {
            if (elements instanceof THashMap) {
                THashMap hashMap = (THashMap) elements;
                hashMap.trimToSize();
            }
        }
    }

    public static <T> void forEach(@Nullable Iterable<T> iterable, @NotNull Consumer<? super T> action) {
        if (iterable != null) {
            if (iterable instanceof List) {
                // indexed loop is supposed to be fastest
                List<T> list = (List<T>) iterable;
                for (int i = 0; i < list.size(); i++) {
                    T element = list.get(i);
                    action.accept(element);
                }

            } else {
                iterable.forEach(action);
            }
        }
    }

    @NotNull
    @Contract(value = " -> new", pure = true)
    public static <T> List<T> createConcurrentList() {
        return new CopyOnWriteArrayList<>();
    }

    public static <T> boolean isLast(@NotNull List<T> collection, @NotNull T element) {
        return collection.indexOf(element) == collection.size() - 1;
    }

    @Nullable
    public static <T> List<T> filter(@NotNull List<T> list, boolean wrap, boolean ensure, @Nullable Filter<T> filter) {
        if (list.isEmpty() || filter == null || filter.acceptsAll(list)) {
            return wrap ? new ArrayList<>(list) : list;
        } else {
            // implicitly wrapping
            List<T> filteredList = ensure ? new ArrayList<>() : null;
            for (int i = 0; i < list.size(); i++) {
                T element = list.get(i);
                if (filter.accepts(element)) {
                    if (filteredList == null) {
                        filteredList = new ArrayList<>();
                    }
                    filteredList.add(element);
                }
            }
            return filteredList;

/*
            return list.
                    stream().
                    filter(element -> element != null && filter.accepts(element)).
                    collect(Collectors.toList());
*/
        }
    }

    public static <S, T> List<T> map(List<S> list, Function<S, T> mapper) {
        return list.stream().map(mapper).collect(Collectors.toList());
    }

    public static <T> void first(@Nullable List<T> list, Predicate<? super T> predicate, ParametricRunnable<T> callback) {
        if (list != null && !list.isEmpty()) {
            // indexed loop is supposed to be fastest
            for (int i=0; i<list.size(); i++) {
                T element = list.get(i);
                if (predicate.test(element)) {
                    callback.run(element);
                    return;
                }
            }
        }
    }

    public static <T> T first(List<T> list, Predicate<? super T> predicate) {
        if (list != null && !list.isEmpty()) {
            // indexed loop is supposed to be fastest
            for (int i=0; i<list.size(); i++) {
                T element = list.get(i);
                if (predicate.test(element)) {
                    return element;
                }
            }
        }
        return null;
    }
}
