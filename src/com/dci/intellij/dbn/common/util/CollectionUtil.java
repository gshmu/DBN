package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.common.list.FiltrableList;
import gnu.trove.THashSet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class CollectionUtil {
    public static <T extends Cloneable<T>> void cloneCollectionElements(Collection<T> source, Collection<T> target) {
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

    public static void clearMap(Map map) {
        if (map != null) {
            map.clear();
        }
    }

    public static void compactElements(List<? extends Compactable> elements) {
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
    public static void compact(Collection elements) {
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
            }
        }
    }

    public static <T> void forEach(@Nullable Iterable<T> iterable, @NotNull Consumer<? super T> action) {
        if (iterable != null) {
            for (T element : iterable) {
                action.accept(element);
            }
        }
    }

    @NotNull
    @Contract(value = " -> new", pure = true)
    public static <T> List<T> createConcurrentList() {
        return new CopyOnWriteArrayList<>();
    }
}
