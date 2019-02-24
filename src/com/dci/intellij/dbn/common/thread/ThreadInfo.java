package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.property.PropertyHolderImpl;

public class ThreadInfo extends PropertyHolderImpl<ThreadProperty> {
    @Override
    protected ThreadProperty[] properties() {
        return ThreadProperty.values();
    }
}
