package com.dci.intellij.dbn.common.property;

import com.dci.intellij.dbn.common.dispose.DisposableBase;

public abstract class PropertyHolderImpl<T extends Property> extends DisposableBase implements PropertyHolder<T>, Cloneable{
    //private static PrimeNumberIndex INDEX = new PrimeNumberIndex(100);

    private int computed = 0;

    public PropertyHolderImpl() {
        for (T property : getProperties()) {
            if (property.implicit()) {
                set(property);
            }
        }
    }

    protected abstract T[] getProperties();

    @Override
    public boolean set(T property, boolean value) {
        return value ?
                set(property) :
                unset(property);
    }

    @Override
    public boolean is(T property) {
        int idx = property.index();
        return (computed & idx) == idx;
    }

    @Override
    public boolean isNot(T status) {
        return !is(status);
    }

    private boolean set(T property) {
        if (isNot(property)) {
            PropertyGroup group = property.group();
            if (group != null) {
                for (T prop : getProperties()) {
                    if (is(prop)) {
                        computed -= prop.index();
                        break;
                    }
                }
            }

            computed = computed += property.index();
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
                for (T prop : getProperties()) {
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
        for (T property : getProperties()) {
            if (property.implicit()) {
                set(property);
            }
        }
    }

    public static int idx(Enum property) {
        //return INDEX.getPrime(property.ordinal());
        double pow = Math.pow(2, property.ordinal());
        if (pow > Integer.MAX_VALUE) {
            System.out.println(pow);
        }
        return (int) pow;

    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (T property : getProperties()) {
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
    public PropertyHolderImpl clone() {
        try {
            return (PropertyHolderImpl) super.clone();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
