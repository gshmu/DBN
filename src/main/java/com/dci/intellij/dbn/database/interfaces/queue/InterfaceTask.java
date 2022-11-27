package com.dci.intellij.dbn.database.interfaces.queue;

import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.thread.ThreadMonitor;
import com.dci.intellij.dbn.common.util.Exceptions;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.common.util.TimeAware;
import lombok.Getter;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.Comparator;
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
    private final StatusHolder<InterfaceTaskStatus> status = new StatusHolder<>(NEW);

    private R response;
    private Throwable exception;

    InterfaceTask(InterfaceTaskDefinition info, boolean synchronous, ThrowableCallable<R, SQLException> executor) {
        this.info = info;
        this.executor = executor;
        this.synchronous = synchronous;
    }

    final R execute() {
        try {
            status.change(STARTED);
            this.response = executor.call();
        } catch (Throwable exception) {
            this.exception = exception;
        } finally {
            status.change(FINISHED);
            LockSupport.unpark(thread);
        }
        return this.response;
    }

    final void awaitCompletion() throws SQLException {
        if (!synchronous) {
            status.change(RELEASED);
            return;
        }

        boolean dispatchThread = ThreadMonitor.isDispatchThread();
        boolean modalProcess = ThreadMonitor.isModalProcess();
        while (status.isBefore(FINISHED)) {
            LockSupport.parkNanos(this, dispatchThread ? ONE_SECOND : TEN_SECONDS);

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

        status.change(RELEASED);
        if (exception == null) return;

        throw Exceptions.toSqlException(exception);
    }

    public boolean changeStatus(InterfaceTaskStatus status, Runnable callback) {
        return this.status.change(status, callback);
    }

    public boolean isProgress() {
        return Strings.isNotEmpty(getTitle());
    }
}
