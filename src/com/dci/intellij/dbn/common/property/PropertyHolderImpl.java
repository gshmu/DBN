package com.dci.intellij.dbn.common.property;

import com.dci.intellij.dbn.common.util.Cloneable;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;

public abstract class PropertyHolderImpl<T extends Property> implements PropertyHolder<T>, Cloneable<PropertyHolder<T>> {
    private volatile long computed = 0;

    @SafeVarargs
    public PropertyHolderImpl(T ... properties) {
        for (T property : properties()) {
            if (property.implicit()) {
                set(property);
            }
        }
        if (properties != null) {
            for (T property : properties) {
                set(property);
            }
        }
    }

    protected abstract T[] properties();

    protected synchronized void replace(PropertyHolderImpl<T> source) {
        this.computed = source.computed;
    }

    @Override
    public boolean set(T property, boolean value) {
        return value ?
                set(property) :
                unset(property);
    }

    @Override
    public final boolean is(T property) {
        long idx = property.index();
        return (computed & idx) == idx;
    }

    private synchronized boolean set(T property) {
        if (isNot(property)) {
            PropertyGroup group = property.group();
            if (group != null) {
                for (T prop : properties()) {
                    if (is(prop)) {
                        computed -= prop.index();
                        break;
                    }
                }
            }

            computed += property.index();
            return true;
        }
        return false;
    }

    private synchronized boolean unset(T property) {
        if (is(property)) {
            computed -= property.index();

            PropertyGroup group = property.group();
            if (group != null) {
                // set implicit property
                for (T prop : properties()) {
                    if (prop.group() == group && prop.implicit() && prop != property && !is(prop)) {
                        computed += prop.index();
                        break;
                    }
                }
            }
            return true;
        }
        return false;
    }

    public synchronized void reset() {
        computed = 0;
        for (T property : properties()) {
            if (property.implicit()) {
                set(property);
            }
        }
    }

    public void computed(long computed) {
        this.computed = computed;
    }

    public void merge(@Nullable PropertyHolder<T> source) {
        if (source != null) {
            for (T property : properties()) {
                if (source.is(property)) {
                    set(property, true);
                }
            }
        }
    }

    public void unmerge(@Nullable PropertyHolder<T> source) {
        if (source != null) {
            for (T property : properties()) {
                if (source.is(property)) {
                    set(property, false);
                }
            }
        }
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (T property : properties()) {
            if (is(property)) {
                if (builder.length() > 0) {
                    builder.append(" / ");
                }
                builder.append(property);
            }
        }
        return builder.toString();
    }

    @Override
    @SneakyThrows
    public PropertyHolder<T> clone() {
        return (PropertyHolder<T>) super.clone();
    }
}
