package com.dci.intellij.dbn.common.property;

public class PropertyHolderImpl<T extends Property> implements PropertyHolder<T>{
    private int status = 0;
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
    public boolean set(T status, boolean value) {
        return value ?
                set(status) :
                unset(status);
    }

    @Override
    public boolean is(T status) {
        int idx = status.index();
        return (this.status & idx) == idx;
    }

    private boolean set(T property) {
        if (!is(property)) {
            PropertyGroup group = property.group();
            if (group != null) {
                for (T prop : properties) {
                    if (prop.group() == group) {
                        unset(prop);
                        break;
                    }
                }
            }

            this.status += property.index();
            return true;
        }
        return false;
    }

    private boolean unset(T property) {
        if (is(property)) {
            this.status -= property.index();

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

    public static int idx(Enum status) {
        return (int) Math.pow(2, status.ordinal());
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
