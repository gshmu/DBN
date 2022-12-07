package com.dci.intellij.dbn.common.pool;

import com.dci.intellij.dbn.common.count.Counter;
import com.dci.intellij.dbn.common.count.Counters;

import static com.dci.intellij.dbn.common.count.CounterType.*;

public class ObjectPoolCounters extends Counters {
    public Counter peak() {
        return get(PEAK);
    }

    public Counter waiting() {
        return get(WAITING);
    }

    public Counter reserved() {
        return get(RESERVED);
    }

    public Counter rejected() {
        return get(REJECTED);
    }

    public Counter creating() {
        return get(CREATING);
    }

    @Override
    public String toString() {
        return
            "peak=" + peak().get() + " " +
            "waiting=" + waiting().get() + " " +
            "reserved=" + reserved().get() + " " +
            "rejected=" + rejected().get() + " " +
            "creating=" + creating().get()
                ;
    }
}
