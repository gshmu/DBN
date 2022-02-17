package com.dci.intellij.dbn.common.property;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.dci.intellij.dbn.common.util.Unsafe.cast;

public abstract class PropertyHolderBase<T extends Property> implements PropertyHolder<T> {
    private static final Map<Class<? extends PropertyHolder>, Property[]> REGISTRY = new ConcurrentHashMap<>();

    protected abstract T[] properties();
    protected abstract void change(T property, boolean value);

    private T[] props() {
        try {
            return cast(REGISTRY.computeIfAbsent(getClass(), c -> properties()));
        } catch (IllegalStateException e) {
            // TODO why??
            return cast(REGISTRY.get(getClass()));
        }
    }

    @SafeVarargs
    protected PropertyHolderBase(T ... properties) {
        for (T property : props()) {
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

    @Override
    public boolean set(T property, boolean value) {
        return value ?
                set(property) :
                unset(property);
    }

    protected final boolean set(T property) {
        if (isNot(property)) {
            PropertyGroup group = property.group();
            if (group != null) {
                for (T prop : props()) {
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

    protected boolean unset(T property) {
        if (is(property)) {
            change(property, false);

            PropertyGroup group = property.group();
            if (group != null) {
                // set implicit property
                for (T prop : props()) {
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
        for (T property : props()) {
            if (property.implicit()) {
                set(property);
            }
        }
    }

    public void merge(@Nullable PropertyHolder<T> source) {
        if (source != null) {
            for (T property : props()) {
                if (source.is(property)) {
                    set(property, true);
                }
            }
        }
    }

    public void unmerge(@Nullable PropertyHolder<T> source) {
        if (source != null) {
            for (T property : props()) {
                if (source.is(property)) {
                    set(property, false);
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (T property : props()) {
            if (is(property)) {
                if (builder.length() > 0) {
                    builder.append(" / ");
                }
                builder.append(property);
            }
        }
        return builder.toString();
    }

    public abstract static class IntStore<T extends Property.IntBase> extends PropertyHolderBase<T> {
        private volatile int computed;

        @SafeVarargs
        public IntStore(T ... properties) {
            super(properties);
        }

        protected void replace(IntStore<T> source) {
            this.computed = source.computed;
        }

        public final boolean is(T property) {
            return (computed & property.maskOn()) != 0;
        }

        protected void change(T property, boolean value) {
            // TODO synchronization overhead??
            // noinspection NonAtomicOperationOnVolatileField
            this.computed = value ?
                    this.computed | property.maskOn() :
                    this.computed & property.maskOff();
        }

        public void reset() {
            computed = 0;
            super.reset();
        }
    }

    public abstract static class LongStore<T extends Property.LongBase> extends PropertyHolderBase<T> {
        private volatile long computed;

        @SafeVarargs
        public LongStore(T ... properties) {
            super(properties);
        }

        protected void replace(LongStore<T> source) {
            this.computed = source.computed;
        }

        public final boolean is(T property) {
            return (computed & property.maskOn()) != 0;
        }


        protected void change(T property, boolean value) {
            // TODO synchronization overhead??
            // noinspection NonAtomicOperationOnVolatileField
            this.computed = value ?
                    this.computed | property.maskOn() :
                    this.computed & property.maskOff();
        }

        public void reset() {
            computed = 0;
            super.reset();
        }
    }
}
