package com.dci.intellij.dbn.common.property;

public class PropertyHolderImpl<T extends Property> implements PropertyHolder<T>{
    private int status = 0;
    private Class<T> type;

    public PropertyHolderImpl(Class<T> type) {
        this.type = type;
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

    private boolean set(T status) {
        if (!is(status)) {
            this.status += status.index();
            return true;
        }
        return false;
    }

    private boolean unset(T status) {
        if (is(status)) {
            this.status -= status.index();
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
        T[] properties = type.getEnumConstants();
        if (properties != null) {
            for (T property : properties) {
                if (is(property)) {
                    if (builder.length() > 0) {
                        builder.append(" / ");
                    }
                    builder.append(property.toString());
                }
            }
        }
        return builder.toString();
    }
}
