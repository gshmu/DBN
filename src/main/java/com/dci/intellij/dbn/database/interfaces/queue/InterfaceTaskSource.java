package com.dci.intellij.dbn.database.interfaces.queue;

import com.dci.intellij.dbn.common.thread.ThreadInfo;
import com.dci.intellij.dbn.common.thread.ThreadMonitor;
import lombok.Getter;

@Getter
public class InterfaceTaskSource {
    private final long timestamp = System.currentTimeMillis();
    private final Thread thread = Thread.currentThread();
    private final ThreadInfo threadInfo = ThreadMonitor.current();

    private final boolean waiting; // the source is waiting for the response

    public InterfaceTaskSource(boolean waiting) {
        this.waiting = waiting;
    }
}
