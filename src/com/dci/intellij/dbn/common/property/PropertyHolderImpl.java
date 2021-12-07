package com.dci.intellij.dbn.common.property;

import org.jetbrains.annotations.Nullable;

public abstract class PropertyHolderImpl<T extends Property> implements PropertyHolder<T> {
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

    protected void replace(PropertyHolderImpl<T> source) {
        this.computed = source.computed();
    }

    @Override
    public boolean set(T property, boolean value) {
        return value ?
                set(property) :
                unset(property);
    }

    public final boolean is(T property) {
        return (computed & property.computedOne()) != 0;
    }


    private void change(T property, boolean value) {
        this.computed = value ?
                this.computed | property.computedOne() :
                this.computed & property.computedZero();
    }

    private boolean set(T property) {
        if (isNot(property)) {
            PropertyGroup group = property.group();
            if (group != null) {
                for (T prop : properties()) {
                    if (is(prop)) {
                        change(prop, false);
                        break;
                    }
                }
            }

            change(property, true);
            return true;
        }
        return false;
    }

    private boolean unset(T property) {
        if (is(property)) {
            change(property, false);

            PropertyGroup group = property.group();
            if (group != null) {
                // set implicit property
                for (T prop : properties()) {
                    if (prop.group() == group && prop.implicit() && prop != property && !is(prop)) {
                        change(prop, true);
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

    public long computed() {
        return computed;
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
}
