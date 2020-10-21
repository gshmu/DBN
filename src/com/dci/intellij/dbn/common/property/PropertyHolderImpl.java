package com.dci.intellij.dbn.common.property;

import com.dci.intellij.dbn.common.util.Cloneable;
import com.dci.intellij.dbn.common.util.Unsafe;
import org.jetbrains.annotations.Nullable;

public abstract class PropertyHolderImpl<T extends Property> implements PropertyHolder<T>, Cloneable<PropertyHolder<T>> {
    //private static PrimeNumberIndex INDEX = new PrimeNumberIndex(100);

    private int computed = 0;

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

    protected void replace(PropertyHolderImpl<T> source) {
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
        int idx = property.index();
        return (computed & idx) == idx;
    }

    private boolean set(T property) {
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

    private boolean unset(T property) {
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

    public void reset() {
        computed = 0;
        for (T property : properties()) {
            if (property.implicit()) {
                set(property);
            }
        }
    }

    public void computed(int computed) {
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
                builder.append(property.toString());
            }
        }
        return builder.toString();
    }

    @Override
    public PropertyHolder<T> clone() {
        return Unsafe.call(() -> (PropertyHolder<T>) super.clone());
    }
}
