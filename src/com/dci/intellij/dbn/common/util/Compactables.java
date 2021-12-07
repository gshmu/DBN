package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.common.list.FilteredList;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.dci.intellij.dbn.common.util.Unsafe.cast;

public class Compactables {
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
}
