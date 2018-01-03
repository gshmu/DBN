package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.property.Property;
import com.dci.intellij.dbn.common.property.PropertyGroup;
import com.dci.intellij.dbn.common.property.PropertyHolderImpl;

public enum TaskInstruction implements Property {
    START_IN_BACKGROUND,
    CAN_BE_CANCELLED;

    private final int index = PropertyHolderImpl.idx(this);

    @Override
    public int index() {
        return index;
    }

    @Override
    public PropertyGroup group() {
        return null;
    }

    @Override
    public boolean implicit() {
        return false;
    }
}
