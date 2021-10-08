package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.list.FilteredList;
import com.dci.intellij.dbn.common.routine.ParametricRunnable;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.dci.intellij.dbn.common.util.Unsafe.cast;

public class CollectionUtil {
    public static <T extends Cloneable<T>> void cloneElements(Collection<T> source, Collection<T> target) {
        for (T cloneable : source) {
            T clone = cloneable.clone();
            target.add(clone);
        }
    }

    public static void clear(Collection<?> collection) {
        if (collection != null && collection.size() > 0) {
            try {
                collection.clear();
            } catch (UnsupportedOperationException ignore) {}
        }
    }

    public static void clear(@Nullable Map<?, ?> map) {
        if (map != null) {
            try {
                map.clear();
            } catch (UnsupportedOperationException ignore) {}
        }
    }

    public static <T extends Collection<E>, E extends Compactable> T compactRecursive(@Nullable T elements) {
        if (elements != null) {
            elements = compact(elements);
            for (Compactable element : elements) {
                element.compact();
            }
        }
        return elements;
    }

    public static <T extends Compactable> void compact(@Nullable T compactable) {
        if (compactable != null) {
            compactable.compact();
        }
    }


    public static <T extends Collection<E>, E> T compact(@Nullable T elements) {
        if (elements != null) {
            int size = elements.size();
            boolean empty = size == 0;
            boolean single = size == 1;

            if (elements instanceof FilteredList) {
                FilteredList<?> filteredList = (FilteredList<?>) elements;
                filteredList.trimToSize();

            } else  if (elements instanceof List) {
                if (empty) {
                    return cast(Collections.emptyList());
                } else if (single) {
                    return cast(Collections.singletonList(elements.stream().findFirst().orElse(null)));
                } else if (elements instanceof ArrayList){
                    ArrayList<?> arrayList = (ArrayList<?>) elements;
                    arrayList.trimToSize();
                    return cast(arrayList);
                }
            }  else if (elements instanceof Set) {
                if (empty) {
                    return cast(Collections.emptySet());
                } else if (single) {
                    return cast(Collections.singleton(elements.stream().findFirst().orElse(null)));
                } else if (elements instanceof THashSet){
                    THashSet<?> hashSet = (THashSet<?>) elements;
                    hashSet.trimToSize();
                    return cast(hashSet);
                }
            }
        }
        return elements;
    }

    public static <T extends Map<K, V>, K, V> T compact(@Nullable T elements) {
        if (elements != null) {
            int size = elements.size();
            boolean empty = size == 0;
            boolean single = size == 1;

            if (empty) {
                return cast(Collections.emptyMap());
            } else if (single) {
                K key = elements.keySet().stream().findFirst().orElse(null);
                V value = elements.get(key);
                return cast(Collections.singletonMap(key, value));
            } else if (elements instanceof THashMap) {
                THashMap map = (THashMap) elements;
                map.compact();
            }
        }
        return elements;
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
            for (T element : list) {
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

    public static <T> void first(@Nullable List<T> list, Predicate<? super T> predicate, ParametricRunnable.Basic<T> callback) {
        if (list != null && !list.isEmpty()) {
            for (T element : list) {
                if (predicate.test(element)) {
                    callback.run(element);
                    return;
                }
            }
        }
    }

    public static <T> T first(List<T> list, Predicate<? super T> predicate) {
        if (list != null && !list.isEmpty()) {
            return list.stream().filter(predicate).findFirst().orElse(null);
        }
        return null;
    }

    public static int indexOf(@NotNull List<String> where, @NotNull String what, boolean ignoreCase) {
        int index = where.indexOf(what);
        if (index == -1) {
            for (int i=0; i < where.size(); i++ ) {
                String string = where.get(i);
                if (ignoreCase && StringUtil.equalsIgnoreCase(string, what) ||
                        (!ignoreCase && Objects.equals(string, what))) {
                    return i;
                }
            }
        }
        return index;
    }
}
