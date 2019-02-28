package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.property.Property;

public enum ThreadProperty implements Property{
    CODE_COMPLETION,
    CODE_ANNOTATING,

    TIMEOUT_PROCESS,
    CANCELABLE_PROCESS,
    BACKGROUND_THREAD,
    BACKGROUND_TASK,
    MODAL_TASK
;

    private final int index = Property.idx(this);

    @Override
    public int index() {
        return index;
    }
}
