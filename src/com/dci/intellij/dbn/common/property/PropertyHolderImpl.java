package com.dci.intellij.dbn.common.property;

public class PropertyHolderImpl<T extends Property> implements PropertyHolder<T>{
    private int computed = 0;
    private T[] properties;

    public PropertyHolderImpl(Class<T> type) {
        this.properties = type.getEnumConstants();
        for (T property : properties) {
            if (property.implicit()) {
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

    @Override
    public boolean is(T property) {
        int idx = property.index();
        return (this.computed & idx) == idx;
    }

    @Override
    public boolean isNot(T status) {
        return !is(status);
    }

    private boolean set(T property) {
        if (isNot(property)) {
            PropertyGroup group = property.group();
            if (group != null) {
                for (T prop : properties) {
                    if (prop.group() == group) {
                        unset(prop);
                        break;
                    }
                }
            }

            this.computed += property.index();
            return true;
        }
        return false;
    }

    private boolean unset(T property) {
        if (is(property)) {
            this.computed -= property.index();

            PropertyGroup group = property.group();
            if (group != null) {
                for (T prop : properties) {
                    if (prop.group() == group && prop.implicit()) {
                        set(prop);
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
        for (T property : properties) {
            if (property.implicit()) {
                set(property);
            }
        }
    }

    public static int idx(Enum property) {
        return (int) Math.pow(2, property.ordinal());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (T property : properties) {
            if (is(property)) {
                if (builder.length() > 0) {
                    builder.append(" / ");
                }
                builder.append(property.toString());
            }
        }
        return builder.toString();
    }
}
