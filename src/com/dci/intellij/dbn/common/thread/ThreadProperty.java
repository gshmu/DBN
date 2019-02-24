package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.property.Property;

public enum ThreadProperty implements Property{
    CODE_COMPLETION,
    CODE_ANNOTATING,
    BACKGROUND_PROGRESS,
    BACKGROUND_PROCESS,
    TIMEOUT_PROCESS
;

    private final int index = Property.idx(this);

    @Override
    public int index() {
        return index;
    }
}
