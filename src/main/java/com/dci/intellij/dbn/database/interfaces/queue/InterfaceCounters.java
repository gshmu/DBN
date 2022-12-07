package com.dci.intellij.dbn.database.interfaces.queue;

import com.dci.intellij.dbn.common.count.Counter;
import com.dci.intellij.dbn.common.count.Counters;

import static com.dci.intellij.dbn.common.count.CounterType.*;

public class InterfaceCounters extends Counters {
    public int active() {
        return queued().value() + running().value();
    }

    public Counter running() {
        return get(RUNNING);
    }

    public Counter queued() {
        return get(QUEUED);
    }

    public Counter finished() {
        return get(FINISHED);
    }

    @Override
    public String toString() {
        return "Q=" + queued().value() + " R=" + running().value() + " F=" + finished().value();
    }
}
