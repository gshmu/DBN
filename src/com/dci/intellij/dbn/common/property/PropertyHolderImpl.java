package com.dci.intellij.dbn.common.property;

import org.jetbrains.annotations.Nullable;

public abstract class PropertyHolderImpl<T extends Property> implements PropertyHolder<T> {
    private long computed = 0L;

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
        this.computed = source.computed;
    }

    @Override
    public boolean set(T property, boolean value) {
        return value ?
                set(property) :
                unset(property);
    }

    public final boolean is(T property) {
        return (computed & (1L << property.ordinal())) != 0;
    }

    private boolean set(T property) {
        long computed = this.computed;
        PropertyGroup group = property.group();
        if (group != null) {
            for (T prop : properties()) {
                if (is(prop)) {
                    this.computed |= (1L << prop.ordinal());
                    break;
                }
            }
        }

        this.computed |= (1L << property.ordinal());
        return computed != this.computed;
    }

    private boolean unset(T property) {
        long computed = this.computed;
        this.computed &= ~(1L << property.ordinal());

        PropertyGroup group = property.group();
        if (group != null) {
            // set implicit property
            for (T prop : properties()) {
                if (prop.group() == group && prop.implicit() && prop != property) {
                    this.computed &= ~(1L << prop.ordinal());
                    break;
                }
            }
        }

        return computed != this.computed;
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
