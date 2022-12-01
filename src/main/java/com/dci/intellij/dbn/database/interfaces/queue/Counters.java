package com.dci.intellij.dbn.database.interfaces.queue;

import com.dci.intellij.dbn.common.Counter;

public class Counters {
    public final Counter queued = new Counter();
    public final Counter running = new Counter();
    public final Counter finished = new Counter();

    public int active() {
        return queued.get() + running.get();
    }

    public int running() {
        return running.get();
    }

    @Override
    public String toString() {
        return "Q=" + queued.get() + " R=" + running.get() + " F=" + finished.get();
    }
}
