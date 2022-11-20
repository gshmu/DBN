package com.dci.intellij.dbn.database.interfaces.queue;

import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.thread.ThreadMonitor;
import com.dci.intellij.dbn.common.util.Exceptions;
import com.dci.intellij.dbn.common.util.TimeAware;
import lombok.Getter;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;

import static com.dci.intellij.dbn.database.interfaces.queue.InterfaceTaskStatus.*;

@Slf4j
@Getter
class InterfaceTask<R> implements TimeAware {
    public static final Comparator<InterfaceTask<?>> COMPARATOR = (t1, t2) -> t2.info.getPriority().compareTo(t1.info.getPriority());
    private static final long TEN_SECONDS = TimeUnit.SECONDS.toNanos(10);
    private static final long ONE_SECOND = TimeUnit.SECONDS.toNanos(1);

    @Delegate
    private final InterfaceTaskDefinition info;
    private final boolean synchronous;
    private final ThrowableCallable<R, SQLException> executor;
    private final Thread thread = Thread.currentThread();
    private final long timestamp = System.currentTimeMillis();
    private final Stack<InterfaceTaskStatus> statusHistory = new Stack<>();

    private R response;
    private Throwable exception;
    private volatile InterfaceTaskStatus status = InterfaceTaskStatus.NEW;

    InterfaceTask(InterfaceTaskDefinition info, boolean synchronous, ThrowableCallable<R, SQLException> executor) {
        this.info = info;
        this.executor = executor;
        this.synchronous = synchronous;
    }

    void changeStatus(InterfaceTaskStatus status) {
        statusHistory.push(this.status);
        this.status = status;
    }

    final R execute() {
        try {
            changeStatus(STARTED);
            this.response = executor.call();
        } catch (Throwable exception) {
            this.exception = exception;
        } finally {
            changeStatus(FINISHED);
            LockSupport.unpark(thread);
        }
        return this.response;
    }

    final void awaitCompletion() throws SQLException {
        if (!synchronous) {
            changeStatus(RELEASED);
            return;
        }

        boolean dispatchThread = ThreadMonitor.isDispatchThread();
        while (status.compareTo(FINISHED) < 0) {
            if (isOlderThan(5, TimeUnit.MINUTES))  break;
            LockSupport.parkNanos(dispatchThread ? ONE_SECOND : TEN_SECONDS);

            if (dispatchThread) {
                log.error("Interface loads not allowed from event dispatch thread",
                        new RuntimeException("Illegal database interface invocation"));

                break;
            }

            if (isOlderThan(5, TimeUnit.MINUTES)) {
                exception = new TimeoutException();
                break;
            }
        }

        changeStatus(RELEASED);
        if (exception == null) return;

        throw Exceptions.toSqlException(exception);
    }
}
