package com.dci.intellij.dbn.database.interfaces.queue;

import com.dci.intellij.dbn.common.Counter;

public class Counters {
    private final Counter queued = new Counter();
    private final Counter running = new Counter();
    private final Counter finished = new Counter();

    public Counter queued() {
        return queued;
    }

    public Counter running() {
        return running;
    }

    public Counter finished() {
        return finished;
    }
}
