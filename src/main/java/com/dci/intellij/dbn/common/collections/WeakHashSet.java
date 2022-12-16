package com.dci.intellij.dbn.common.collections;

import com.dci.intellij.dbn.common.exception.Exceptions;
import com.dci.intellij.dbn.common.ref.WeakRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class WeakHashSet<T> implements Set<T> {
    private final Set<WeakRef<T>> data = new HashSet<>();

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return mapped().anyMatch(v -> Objects.equals(v, o));
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return mapped().iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return mapped().toArray();
    }

    @NotNull
    @Override
    public <T1> T1[] toArray(@NotNull T1[] a) {
        return Exceptions.unsupported();
    }

    @Override
    public boolean add(T t) {
        return data.add(WeakRef.of(t));
    }

    @Override
    public boolean remove(Object o) {
        return data.removeIf(d -> Objects.equals(value(d), o));
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return Exceptions.unsupported();
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        return Exceptions.unsupported();
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return Exceptions.unsupported();
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return Exceptions.unsupported();
    }

    @Override
    public void clear() {
        data.clear();
    }

    @Nullable
    private T value(WeakRef<T> d) {
        return WeakRef.get(d);
    }

    @NotNull
    private Stream<T> mapped() {
        return data.stream().map(d -> value(d));
    }

}
