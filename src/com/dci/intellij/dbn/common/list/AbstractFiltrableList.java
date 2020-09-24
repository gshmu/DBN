package com.dci.intellij.dbn.common.list;

import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public abstract class AbstractFiltrableList<T> implements FiltrableList<T> {
    private List<T> list;

    public AbstractFiltrableList() {
        list = new ArrayList<T>();
    }

    public AbstractFiltrableList(List<T> list) {
        this.list = list;
    }

    @Override
    public List<T> getFullList() {return list;}

    // update methods should not be affected by filtering
    @Override
    public void sort(Comparator<? super T> comparator)          {
        list.sort(comparator);}

    @Override
    public boolean add(T o) {
        return list.add(o);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        return list.addAll(c);
    }

    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }

    @Override
    public boolean removeAll(@NotNull Collection c) {
        return list.removeAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection c) {
        return list.retainAll(c);
    }

    @Override
    public void clear() {
        if (!list.isEmpty()) list.clear();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    private boolean isFiltered() {
        return getFilter() != null;
    }

    @Override
    public int size() {
        Filter<T> filter = getFilter();
        if (filter != null) {
            int count = 0;
            for (T object : list) if (filter.accepts(object)) count++;
            return count;
        } else {
            return list.size();

        }
    }

    @Override
    @NotNull
    public Iterator<T> iterator(){
        final Filter<T> filter = getFilter();
        if (filter != null) {
            return new Iterator<T>() {
                private final Iterator<T> iterator = list.iterator();
                private T next = findNext();
                private T findNext() {
                    while (iterator.hasNext()) {
                        next = iterator.next();
                        if (filter.accepts(next)) return next;
                    }
                    return null;
                }

                @Override
                public boolean hasNext() { return next != null;}
                @Override
                public T next() {
                    T result = next;
                    next = findNext();
                    return result;
                }
                @Override
                public void remove(){
                    throw new UnsupportedOperationException("Iterator remove not implemented in filtrable actions");
                }
            };
        } else {
            return list.iterator();

        }
    }

    @Override
    @NotNull
    public Object[] toArray() {
        Filter<T> filter = getFilter();
        if (filter != null) {
            List<T> result = new ArrayList<T>();
            for (T object : list) if (filter.accepts(object)) result.add(object);
            return result.toArray();
        } else {
            return list.toArray();
        }
    }

    @Override
    @NotNull
    public <E> E[] toArray(@NotNull E[] e) {
        Filter<T> filter = getFilter();
        if (filter != null) {
            List<T> result = new ArrayList<T>();
            for (T object : list) if (filter.accepts(object)) result.add(object);
            return result.toArray(e);
        } else {
            return list.toArray(e);
        }

    }

    @Override
    public boolean contains(Object o){
        if (isFiltered()) {
            return indexOf(o) > -1;
        } else {
            return list.contains(o);
        }

    }

    @Override
    public int indexOf(Object o) {
        Filter<T> filter = getFilter();
        if (filter != null) {
            if (!filter.accepts((T) o)) return -1;

            int index = 0;
            for (T object : list) {
                if (object.equals(o)) return index;
                if (filter.accepts(object)) index++;
            }
            return -1;
        } else {
            return list.indexOf(o);
        }
    }

    @Override
    public int lastIndexOf(Object o) {
        Filter<T> filter = getFilter();
        if (filter != null) {
            if (!filter.accepts((T) o)) return -1;

            int index = size()-1;
            for (int i = list.size()-1; i > -1; i--) {
                T object = list.get(i);
                if (object.equals(o)) return index;
                if (filter.accepts(object)) index--;
            }
            return -1;
        } else {
            return list.lastIndexOf(o);
        }

    }

    @Override
    public boolean containsAll(@NotNull Collection c) {
        if (isFiltered()) {
            List list = Arrays.asList(toArray());
            return list.containsAll(c);
        } else {
            return list.containsAll(c);
        }
    }

    public boolean equals(Object o){
        if (isFiltered()) {
            List list = Arrays.asList(toArray());
            return list.equals(o);
        } else {
            return list.equals(o);
        }

    }

    private int findIndex(int index) {
        int count = -1;
        Filter<T> filter = getFilter();
        for (int i = 0; i < list.size(); i++) {
            T object = list.get(i);
            if (filter == null || filter.accepts(object)) count++;
            if (count == index) return i;
        }
        return -1;
    }

    @Override
    public void add(int index, T element){
        if (isFiltered()) {
            int idx = findIndex(index);
            list.add(idx, element);
        } else {
            list.add(index, element);
        }
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends T> c){
        if (isFiltered()) {
            int idx = findIndex(index);
            return list.addAll(idx, c);
        } else {
            return list.addAll(index, c);
        }
    }

    @Override
    public T get(int index){
        if (isFiltered()) {
            int idx = findIndex(index);
            return idx == -1 ? null : list.get(idx);
        } else {
            return list.get(index);
        }
    }

    @Override
    public T set(int index, T element) {
        if (isFiltered()) {
            int idx = findIndex(index);
            list.set(idx, element);
            return list.get(idx);
        } else {
            return list.set(index, element);
        }
    }

    @Override
    public T remove(int index) {
        if (isFiltered()) {
            int idx = findIndex(index);
            return list.remove(idx);
        } else {
            return list.remove(index);
        }
    }

    @Override
    @NotNull
    public ListIterator<T> listIterator() {
        throw new UnsupportedOperationException("List iterator not implemented in filtrable actions");
    }

    @Override
    @NotNull
    public ListIterator<T> listIterator(int index) {
        throw new UnsupportedOperationException("List iterator not implemented in filtrable actions");
    }

    @Override
    @NotNull
    public List<T> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("Sublist not implemented in filtrable actions");
    }

    @Override
    public void trimToSize() {
        list = CollectionUtil.compact(list);
    }
}
