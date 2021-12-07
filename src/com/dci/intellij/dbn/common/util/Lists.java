package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.common.filter.Filter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class Lists {
    public static <T> boolean isLast(@NotNull List<T> collection, @NotNull T element) {
        return collection.indexOf(element) == collection.size() - 1;
    }

    public static <T> List<T> filter(@NotNull List<T> list, Predicate<T> predicate) {
        ArrayList<T> result = new ArrayList<>();
        for (T element : list) {
            if (predicate.test(element)) {
                result.add(element);
            }
        }
        return result;
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

    @NotNull
    public static <S, T> List<T> convert(@NotNull List<S> list, Function<S, T> mapper) {
        List<T> result = new ArrayList<>();
        for (S s : list) {
            T value = mapper.apply(s);
            result.add(value);
        }
        return result;
    }

    @Nullable
    public static <T> T first(List<T> list, Predicate<? super T> predicate) {
        if (list != null && !list.isEmpty()) {
            for (T element : list) {
                if (predicate.test(element)) {
                    return element;
                }
            }
        }
        return null;
    }

    public static boolean contains(List<>)

    public static int indexOf(@NotNull List<String> where, @NotNull String what, boolean ignoreCase) {
        int index = where.indexOf(what);
        if (index == -1) {
            for (int i=0; i < where.size(); i++ ) {
                String string = where.get(i);
                if (ignoreCase && Strings.equalsIgnoreCase(string, what) ||
                        (!ignoreCase && Objects.equals(string, what))) {
                    return i;
                }
            }
        }
        return index;
    }
}
