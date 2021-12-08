package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.property.Property;

public enum ThreadProperty implements Property.IntBase {
    CODE_COMPLETION (true),
    CODE_ANNOTATING (true),

    TIMEOUT    (true),
    CANCELABLE (true),
    BACKGROUND (false),
    PROGRESS   (false),
    MODAL      (false)

;
    private final Computed computed = new Computed(this);
    private final boolean propagatable;

    ThreadProperty(boolean propagatable) {
        this.propagatable = propagatable;
    }

    @Override
    public Computed computed() {
        return computed;
    }

    public boolean propagatable() {
        return propagatable;
    }
}
