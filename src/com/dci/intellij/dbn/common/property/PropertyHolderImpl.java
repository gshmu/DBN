package com.dci.intellij.dbn.common.property;

public class PropertyHolderImpl<T extends Property> implements PropertyHolder<T>{
    private int status = 0;

    @Override
    public void set(T status, boolean value) {
        if (value) set(status); else unset(status);
    }

    @Override
    public boolean is(T status) {
        int idx = status.idx();
        return (this.status & idx) == idx;
    }

    private void set(T status) {
        if (!is(status)) {
            this.status += status.idx();
        }
    }

    private void unset(T status) {
        if (is(status)) {
            this.status -= status.idx();
        }
    }

    public static int idx(Enum status) {
        return (int) Math.pow(2, status.ordinal());
    }
}
