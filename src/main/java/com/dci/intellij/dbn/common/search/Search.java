package com.dci.intellij.dbn.common.search;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class Search {
    private Search() {}


    public static <T> T binarySearch(@Nullable List<T> list, SearchAdapter<T> adapter) {
        if (list != null && list.size() > 0) {
            return binarySearch(list, 0, list.size() - 1, adapter);
        }
        return null;
    }

    public static <T> T binarySearch(@Nullable List<T> list, int left, int right, SearchAdapter<T> adapter) {
        if (list != null && list.size() > 0) {
            while (left <= right) {
                int mid = left + right >>> 1;
                T midVal = list.get(mid);
                int result = adapter.evaluate(midVal);
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
                int result = adapter.evaluate(midVal);
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


    public static <T> T linearSearch(List<T> list, SearchAdapter<T> adapter) {
        if (list != null && !list.isEmpty()) {
            for (T element : list) {
                int result = adapter.evaluate(element);
                if (result >= 0) {
                    if (result == 0) {
                        return element;
                    }
                } else {
                    return null;
                }

            }
        }
        return null;
    }

    public static <T> T comboSearch(List<T> list, SearchAdapter<T> linear, SearchAdapter<T> binary) {
        if (list != null && !list.isEmpty()) {
            int index = 0;

            while (index < list.size()) {
                T element = list.get(index);
                int result = linear.evaluate(element);
                if (result >= 0) {
                    if (result == 0) {
                        return element;
                    }
                    index++;
                } else {
                    break;
                }
            }
            return binarySearch(list, index, list.size() - 1, binary);
        }
        return null;
    }
}
