package com.dci.intellij.dbn.database.interfaces;

import com.dci.intellij.dbn.common.Counter;
import com.dci.intellij.dbn.common.Priority;
import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;
import com.dci.intellij.dbn.common.thread.Threads;
import com.dci.intellij.dbn.common.util.Consumer;
import com.dci.intellij.dbn.common.util.Exceptions;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionRef;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

@Slf4j
public class DatabaseInterfaceQueueImpl extends StatefulDisposable.Base implements DatabaseInterfaceQueue{
    private static final ExecutorService MONITORS = Threads.newCachedThreadPool("DBN - Database Interface Monitor", true);

    private final BlockingQueue<InterfaceTask<?>> queue = new PriorityBlockingQueue<>(11, (t1, t2) -> t2.priority.compareTo(t1.priority));
    private final Consumer<InterfaceTask<?>> executor;
    private final Counter activeTasks = new Counter();
    private final Counter finishedTasks = new Counter();
    private final ConnectionRef connection;
    private volatile boolean stopped;


    public DatabaseInterfaceQueueImpl(ConnectionHandler connection) {
        this(connection, task -> {throw new UnsupportedOperationException();});
    }

    DatabaseInterfaceQueueImpl(@Nullable ConnectionHandler connection, Consumer<InterfaceTask<?>> executor) {
        this.connection = ConnectionRef.of(connection);
        this.executor = executor;
        activeTasks.addListener(value -> {
            if (value > maxActiveTasks())
                log.warn("Active task limit exceeded: {} (expected max {})", value, maxActiveTasks());
        });
        MONITORS.submit(this::monitorQueue);
    }

    public int size() {
        return queue.size();
    }

    public int maxActiveTasks() {
        if (connection == null) return 10;

        ConnectionHandler connection = this.connection.ensure();
        return connection.getSettings().getDetailSettings().getMaxConnectionPoolSize();
    }

    @Override
    public Counter activeTasks() {
        return activeTasks;
    }

    @Override
    public Counter finishedTasks() {
        return finishedTasks;
    }

    @Override
    public <R> Callable<R> callableOf(InterfaceTask<R> task) {
        return () -> {
            try {
                return task.execute();
            } finally {
                activeTasks.decrement();
                finishedTasks.increment();
            }
        };
    }

    @Override
    public <R> R scheduleAndReturn(Priority priority, ThrowableCallable<R, SQLException> callable) throws SQLException {
        return queue(priority, true, callable).response;
    }

    @Override
    public void scheduleAndWait(Priority priority, ThrowableRunnable<SQLException> runnable) throws SQLException {
        queue(priority, true, runnable.asCallable());
    }

    @Override
    public void scheduleAndForget(Priority priority, ThrowableRunnable<SQLException> runnable) throws SQLException {
        queue(priority, false, runnable.asCallable());
    }

    @NotNull
    private <T> InterfaceTask<T> queue(Priority priority, boolean synchronous, ThrowableCallable<T, SQLException> callable) throws SQLException {
        InterfaceTask<T> task = new InterfaceTask<>(priority, synchronous, callable);
        queue.add(task);
        task.exit();
        return task;
    }



    private void monitorQueue() {
        Thread monitor = Thread.currentThread();
        activeTasks.addListener(value -> {
            if (value < maxActiveTasks()) {
                LockSupport.unpark(monitor);
            }
        });

        while (!stopped) {
            InterfaceTask<?> task = nextTask();
            if (task == null) continue;
            activeTasks.increment();
            executor.accept(task);
        }
    }

    @SneakyThrows
    private InterfaceTask<?> nextTask() {
        checkDisposed();
        if (activeTasks.get() >= maxActiveTasks()) {
            LockSupport.park();
            return null;
        }
        return queue.poll(1, TimeUnit.MILLISECONDS);
    }

    static class InterfaceTask<R> {
        private final Thread thread = Thread.currentThread();
        private final Priority priority;
        private final ThrowableCallable<R, SQLException> executor;
        private final boolean synchronous;

        private R response;
        private Throwable exception;
        private volatile boolean finished;

        InterfaceTask(Priority priority, boolean synchronous, ThrowableCallable<R, SQLException> executor) {
            this.priority = priority;
            this.executor = executor;
            this.synchronous = synchronous;
        }

        private R execute() {
            try {
                this.response = executor.call();
            } catch (Throwable exception) {
                this.exception = exception;
            } finally {
                complete();
            }
            return this.response;
        }

        void complete() {
            finished = true;
            LockSupport.unpark(thread);
        }

        void exit() throws SQLException {
            if (finished || !synchronous) return;

            LockSupport.park();

            if (exception == null) return;
            throw Exceptions.toSqlException(exception);
        }
    }

    @Override
    protected void disposeInner() {
        stopped = true;
        queue.clear();
    }
}
