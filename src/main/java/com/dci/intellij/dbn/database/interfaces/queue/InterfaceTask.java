package com.dci.intellij.dbn.database.interfaces.queue;

import com.dci.intellij.dbn.common.Priority;
import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.util.Exceptions;
import lombok.Getter;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.concurrent.locks.LockSupport;

@Getter
class InterfaceTask<R> {
    public static final Comparator<InterfaceTask<?>> COMPARATOR = (t1, t2) -> t2.getPriority().compareTo(t1.getPriority());

    private final Thread thread = Thread.currentThread();
    private final String title;
    private final Priority priority;
    private final ThrowableCallable<R, SQLException> executor;
    private final boolean synchronous;

    private R response;
    private Throwable exception;
    private volatile boolean finished;

    InterfaceTask(String title, Priority priority, boolean synchronous, ThrowableCallable<R, SQLException> executor) {
        this.title = title;
        this.priority = priority;
        this.executor = executor;
        this.synchronous = synchronous;
    }

    final R execute() {
        try {
            this.response = executor.call();
        } catch (Throwable exception) {
            this.exception = exception;
        } finally {
            complete();
            completed();
        }
        return this.response;
    }

    final void complete() {
        finished = true;
        LockSupport.unpark(thread);
    }

    final void exit() throws SQLException {
        if (finished || !synchronous) return;

        LockSupport.park();

        if (exception == null) return;
        throw Exceptions.toSqlException(exception);
    }

    void completed() {
    }
}
