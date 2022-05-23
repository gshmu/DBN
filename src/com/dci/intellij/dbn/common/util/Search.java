package com.dci.intellij.dbn.common.util;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public final class Search {
    private Search() {}


    public static <T> T binarySearch(@Nullable List<T> list, SearchAdapter<T> adapter) {
        if (list != null && list.size() > 0) {
            int left = 0;
            int right = list.size() - 1;

            while (left <= right) {
                int mid = left + right >>> 1;
                T midVal = list.get(mid);
                int result = adapter.compare(midVal);
                if (result < 0) {
                    left = mid + 1;
                } else if (result > 0){
                    right = mid - 1;
                } else {
                    return list.get(mid);
                }
            }
        }
        return null;
    }

    public static <T> T binarySearch(@Nullable T[] array, SearchAdapter<T> adapter) {
        if (array != null && array.length > 0) {
            int left = 0;
            int right = array.length - 1;

            while (left <= right) {
                int mid = left + right >>> 1;
                T midVal = array[mid];
                int result = adapter.compare(midVal);
                if (result < 0) {
                    left = mid + 1;
                } else if (result > 0){
                    right = mid - 1;
                } else {
                    return array[mid];
                }
            }
        }
        return null;
    }


    public static <T> T linearSearch(List<T> list, Function<T, Boolean> match, Function<T, Boolean> condition) {
        if (list != null && !list.isEmpty()) {
            for (T element : list) {
                if (condition.apply(element)) {
                    if (match.apply(element)) {
                        return element;
                    }
                } else {
                    return null;
                }

            }
        }
        return null;
    }
}
