package com.dci.intellij.dbn.common.util;

import java.util.List;
import java.util.function.Function;

public final class Search {
    private Search() {}


    public static <T> T binarySearch(List<T> list, SearchAdapter<T> adapter) {
        int left = 0;
        int right = list.size() - 1;

        while (left <= right) {
            int mid = left + right >>> 1;
            T midVal = list.get(mid);
            int comparison = adapter.compare(midVal);
            if (comparison < 0) {
                left = mid + 1;
            } else {
                if (comparison <= 0) {
                    return list.get(mid);
                }

                right = mid - 1;
            }
        }
        return null;
    }


    public static <T> T linearSearch(List<T> list, Function<T, Boolean> match, Function<T, Boolean> condition) {
        int index = 0;
        T element = list.get(index);
        while (condition.apply(element)) {
            if (match.apply(element)) {
                return element;
            }
            index++;
            element = list.get(index);
        }
        return null;
    }
}
