package com.dci.intellij.dbn.common.property;

import com.dci.intellij.dbn.common.util.Cloneable;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicLong;

public abstract class PropertyHolderImpl<T extends Property> implements PropertyHolder<T>, Cloneable<PropertyHolder<T>> {
    // TODO consider AtomicInteger for lower memory footprint (properties with up to 32 values)
    private final AtomicLong computed = new AtomicLong(0);

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
        this.computed.set(source.computed.get());
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
        return (computed.get() & idx) == idx;
    }

    private boolean set(T property) {
        if (isNot(property)) {
            PropertyGroup group = property.group();
            if (group != null) {
                for (T prop : properties()) {
                    if (is(prop)) {
                        computed.addAndGet(-prop.index());
                        break;
                    }
                }
            }

            computed.addAndGet(property.index());
            return true;
        }
        return false;
    }

    private boolean unset(T property) {
        if (is(property)) {
            computed.addAndGet(-property.index());

            PropertyGroup group = property.group();
            if (group != null) {
                // set implicit property
                for (T prop : properties()) {
                    if (prop.group() == group && prop.implicit() && prop != property && !is(prop)) {
                        computed.addAndGet(prop.index());
                        break;
                    }
                }
            }
            return true;
        }
        return false;
    }

    public void reset() {
        computed.set(0);
        for (T property : properties()) {
            if (property.implicit()) {
                set(property);
            }
        }
    }

    public void computed(long computed) {
        this.computed.set(computed);
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
    @SneakyThrows
    public PropertyHolder<T> clone() {
        return (PropertyHolder<T>) super.clone();
    }
}
