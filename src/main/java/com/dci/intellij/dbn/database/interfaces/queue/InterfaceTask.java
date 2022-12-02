package com.dci.intellij.dbn.database.interfaces.queue;

import com.dci.intellij.dbn.common.exception.Exceptions;
import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.thread.ThreadMonitor;
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
    public static final Comparator<InterfaceTask<?>> COMPARATOR = (t1, t2) -> t2.request.getPriority().compareTo(t1.request.getPriority());
    private static final long TEN_SECONDS = TimeUnit.SECONDS.toNanos(10);
    private static final long ONE_SECOND = TimeUnit.SECONDS.toNanos(1);

    @Delegate
    private final InterfaceTaskRequest request;
    private final InterfaceTaskSource source;
    private final ThrowableCallable<R, SQLException> executor;
    private final StatusHolder<InterfaceTaskStatus> status = new StatusHolder<>(NEW);

    private R response;
    private Throwable exception;

    InterfaceTask(InterfaceTaskRequest request, boolean synchronous, ThrowableCallable<R, SQLException> executor) {
        this.request = request;
        this.executor = executor;
        this.source = new InterfaceTaskSource(synchronous);
    }

    @Override
    public long getTimestamp() {
        return source.getTimestamp();
    }

    final R execute() {
        try {
            status.change(STARTED);
            this.response = executor.call();
        } catch (Throwable exception) {
            this.exception = exception;
        } finally {
            status.change(FINISHED);
            LockSupport.unpark(source.getThread());
        }
        return this.response;
    }

    final void awaitCompletion() throws SQLException {
        if (!source.isWaiting()) {
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

        if (exception == null) return;

        throw Exceptions.toSqlException(exception);
    }

    public boolean is(InterfaceTaskStatus status) {
        return this.status.is(status);
    }

    public boolean changeStatus(InterfaceTaskStatus status) {
        return this.status.change(status);
    }

    public boolean isProgress() {
        return Strings.isNotEmpty(getTitle());
    }
}
